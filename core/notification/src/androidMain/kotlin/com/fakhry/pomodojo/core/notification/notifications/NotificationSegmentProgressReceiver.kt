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
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
                val sessionRepository: ActiveSessionRepository = koin.get()

                val session = withContext(Dispatchers.IO) {
                    sessionRepository.getActiveSession()
                }

                if (session == PomodoroSessionDomain()) {
                    Log.i(TAG, "onReceive: no active session")
                    pendingResult.finish()
                    return@launch
                }
                val now = System.currentTimeMillis()

                when (action) {
                    ACTION_SEGMENT_COMPLETE -> {
                        // Process the session to advance segments if current one is completed
                        val updatedSession = processSessionCompletion(session, now)

                        // Update the session in repository if it was modified
                        if (updatedSession != session) {
                            Log.i(TAG, "onReceive: session updated")
                            withContext(Dispatchers.IO) {
                                sessionRepository.saveActiveSession(updatedSession)
                            }
                        }

                        // Schedule notification with the updated session
                        notifier.schedule(updatedSession)
                        provideSoundPlayer().playSegmentCompleted()
                    }

                    ACTION_PROGRESS_UPDATE -> {
                        // Just refresh the notification to update progress bar
                        Log.i(TAG, "onReceive: progress updated")
                        notifier.schedule(session)
                    }
                }
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

/**
 * Processes the session when a segment-completion alarm fires.
 *
 * The phase-transition gate means a finished phase must NOT auto-advance to the next
 * one: the running segment is finalized to COMPLETED, leaving the following segment
 * INITIAL (the parked gate). The user resumes by opening the app and tapping continue.
 * Returns the updated session if the running segment was finalized, else the original.
 */
private fun processSessionCompletion(
    session: PomodoroSessionDomain,
    now: Long,
): PomodoroSessionDomain {
    val segments = session.timeline.segments.toMutableList()
    if (segments.isEmpty()) return session

    // Find the active segment index
    var activeIndex = segments.indexOfFirst {
        it.timerStatus == TimerStatusDomain.RUNNING || it.timerStatus == TimerStatusDomain.PAUSED
    }
    if (activeIndex == -1) {
        activeIndex = segments.indexOfFirst { it.timerStatus != TimerStatusDomain.COMPLETED }
    }
    if (activeIndex == -1) return session // All segments completed

    val activeSegment = segments[activeIndex]
    if (activeSegment.timerStatus != TimerStatusDomain.RUNNING) return session

    val remaining = (activeSegment.timer.finishedInMillis - now).coerceAtLeast(0L)
    if (remaining > 0L) return session

    Log.i(TAG, "processSessionCompletion: segment $activeIndex completed, parking at gate")
    segments[activeIndex] = finalizeSegment(activeSegment)
    return session.copy(
        timeline = TimelineDomain(
            segments = segments,
            hourSplits = session.timeline.hourSplits,
        ),
    )
}

/**
 * Finalizes a segment by setting its status to COMPLETED and clamping progress to 1.0
 */
private fun finalizeSegment(segment: TimerSegmentsDomain): TimerSegmentsDomain {
    val timer = segment.timer.copy(
        progress = segment.timer.progress.coerceAtMost(1f),
        startedPauseTime = 0L,
    )
    return segment.copy(timer = timer, timerStatus = TimerStatusDomain.COMPLETED)
}
