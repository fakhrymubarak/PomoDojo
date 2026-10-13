package com.fakhry.pomodojo.core.notification.notifications

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.fakhry.pomodojo.core.notification.PomodoroSessionNotifier
import com.fakhry.pomodojo.core.notification.audio.provideSoundPlayer
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.elapsedAt
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext

private const val TAG = "NotificationSegmentProgressReceiver"

/**
 * BroadcastReceiver that handles segment completion alarms.
 * Triggered by AlarmManager when a pomodoro segment is expected to complete.
 */
class NotificationSegmentProgressReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION_SEGMENT_COMPLETE && action != ACTION_PROGRESS_UPDATE) return

        // Check notification permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val pendingResult = goAsync()

        scope.launch {
            try {
                // Initialize dependencies
                val koin = GlobalContext.get()
                val notifier: PomodoroSessionNotifier = koin.get<AndroidFocusSessionNotifier>()
                val sessionStore: ActiveSessionStore = koin.get()
                val now = System.currentTimeMillis()

                val session = withContext(Dispatchers.IO) {
                    when (action) {
                        // A finished Segment parks at the Gate; the user continues in the app.
                        ACTION_SEGMENT_COMPLETE -> sessionStore.update { stored ->
                            stored?.copy(timeline = stored.timeline.elapsedAt(now))
                        }

                        else -> sessionStore.session.first()
                    }
                }
                if (session == null) {
                    Log.i(TAG, "onReceive: no active session")
                    return@launch
                }

                notifier.schedule(session)
                if (action == ACTION_SEGMENT_COMPLETE) provideSoundPlayer().playSegmentCompleted()
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "Failed to schedule segment completion alarm")
                e.printStackTrace()
            } finally {
                Log.i(TAG, "onReceive: finally finish pending result")
                pendingResult.finish()
            }
        }
    }

    companion object Companion {
        const val ACTION_SEGMENT_COMPLETE = "com.fakhry.pomodojo.SEGMENT_COMPLETE"
        const val ACTION_PROGRESS_UPDATE = "com.fakhry.pomodojo.PROGRESS_UPDATE"
        const val EXTRA_SESSION_ID = "session_id"
    }
}
