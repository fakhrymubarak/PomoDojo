package com.fakhry.pomodojo.focus.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fakhry.pomodojo.R
import com.fakhry.pomodojo.focus.data.db.AndroidFocusDatabaseHolder
import com.fakhry.pomodojo.focus.domain.model.ActiveFocusSessionDomain
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier

private const val CHANNEL_ID = "focus_session_channel"
private const val CHANNEL_NAME = "Focus Sessions"
private const val REQUEST_CODE_OFFSET = 42_000
private const val EXTRA_SESSION_ID = "session_id"

actual fun provideFocusSessionNotifier(): FocusSessionNotifier {
    val context = AndroidFocusDatabaseHolder.requireContext()
    return AndroidFocusSessionNotifier(context)
}

class AndroidFocusSessionNotifier(
    private val context: Context,
) : FocusSessionNotifier {
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override suspend fun schedule(snapshot: ActiveFocusSessionDomain) {
//        if (snapshot.status != FocusTimerStatus.RUNNING) {
//            cancel(snapshot.sessionId)
//            return
//        }
//        ensureChannel()
//        val triggerAt = snapshot.updatedAtEpochMs + snapshot.phaseRemainingSeconds * 1_000L
//        val pendingIntent = buildPendingIntent(snapshot.sessionId)
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            triggerAt,
//            pendingIntent,
//        )
    }

    override suspend fun cancel(sessionId: String) {
        alarmManager.cancel(buildPendingIntent(sessionId))
        NotificationManagerCompat.from(context).cancel(sessionNotificationId(sessionId))
    }

    private fun ensureChannel() {
        val manager = NotificationManagerCompat.from(context)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel =
                NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName(CHANNEL_NAME)
                    .setDescription("Focus session reminders")
                    .build()
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun buildCompletedNotification(
            context: Context,
            sessionId: String,
        ) {
            val notification =
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(context.getString(R.string.focus_session_complete_title))
                    .setContentText(context.getString(R.string.focus_session_complete_body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
            NotificationManagerCompat.from(context).notify(sessionNotificationId(sessionId), notification)
        }

        private fun sessionNotificationId(sessionId: String): Int = REQUEST_CODE_OFFSET + sessionId.hashCode()
    }

    private fun buildPendingIntent(sessionId: String): PendingIntent {
        val intent =
            Intent(context, FocusSessionNotificationReceiver::class.java).apply {
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            sessionNotificationId(sessionId),
            intent,
            flags,
        )
    }
}

class FocusSessionNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        val sessionId = intent?.getStringExtra(EXTRA_SESSION_ID) ?: return
        AndroidFocusSessionNotifier.buildCompletedNotification(context, sessionId)
    }
}
