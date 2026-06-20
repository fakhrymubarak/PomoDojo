package com.fakhry.pomodojo.core.notification.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.fakhry.pomodojo.domain.pomodoro.model.notification.NotificationSummary

private const val TAG = "AndroidAlarmManager"

class AndroidPomodojoAlarmManager(
    private val context: Context,
    private val alarmManager: AlarmManager?,
) : PomodojoAlarmManager {

    override fun schedule(summary: NotificationSummary) {
        val now = System.currentTimeMillis()

        // Schedule alarms for segment completion and periodic progress updates
        if (!summary.isPaused && summary.finishTimeMillis > 0) {
            scheduleProgressUpdateAlarm(summary.sessionId, now)
            scheduleSegmentCompletionAlarm(summary.sessionId, summary.finishTimeMillis)
        } else {
            cancelProgressUpdateAlarm(summary.sessionId)
            cancelSegmentCompletionAlarm(summary.sessionId)
        }
    }

    override fun cancel(sessionId: String) {
        cancelSegmentCompletionAlarm(sessionId)
    }

    private fun scheduleSegmentCompletionAlarm(sessionId: String, finishTime: Long) {
        val intent = Intent(context, NotificationSegmentProgressReceiver::class.java).apply {
            action = NotificationSegmentProgressReceiver.ACTION_SEGMENT_COMPLETE
            putExtra(NotificationSegmentProgressReceiver.EXTRA_SESSION_ID, sessionId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            completedAlarmRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Schedule exact alarm to fire when segment completes
        try {
            scheduleExactAlarm(finishTime, pendingIntent)
        } catch (e: Exception) {
            // Silently fail - notification will still update via ViewModel
            Log.e(TAG, e.message ?: "Failed to schedule segment completion alarm")
        }
    }

    private fun cancelSegmentCompletionAlarm(sessionId: String) {
        val intent = Intent(context, NotificationSegmentProgressReceiver::class.java).apply {
            action = NotificationSegmentProgressReceiver.ACTION_SEGMENT_COMPLETE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            completedAlarmRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager?.cancel(pendingIntent)
    }

    // 'now' is kept for the in-progress periodic-update alarm (see commented body / TODO below)
    @Suppress("UnusedParameter")
    private fun scheduleProgressUpdateAlarm(sessionId: String, now: Long) {
        val intent = Intent(context, NotificationSegmentProgressReceiver::class.java).apply {
            action = NotificationSegmentProgressReceiver.ACTION_PROGRESS_UPDATE
            putExtra(NotificationSegmentProgressReceiver.EXTRA_SESSION_ID, sessionId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            progressAlarmRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // TODO : Fix this bug. It loops the notification update, meanwhile it should only do it once.
        // 1. Re-draw the flowchart diagram of notification and alarm triggers.

        // Schedule exact alarm to fire when segment progress updates
//        val triggerTime = now + PROGRESS_UPDATE_INTERVAL_MS
//        try {
//            scheduleExactAlarm(triggerTime, pendingIntent)
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to schedule progress update alarm", e)
//        }
    }

    @Throws(Exception::class)
    private fun scheduleExactAlarm(triggerAtMillis: Long, operation: PendingIntent) {
        Log.i(TAG, "scheduleExactAlarm at $triggerAtMillis")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager?.canScheduleExactAlarms() == true) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    operation,
                )
            } else {
                // Fallback to inexact alarm if exact alarms not permitted
                alarmManager?.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    operation,
                )
            }
        } else {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                operation,
            )
        }
    }

    private fun cancelProgressUpdateAlarm(sessionId: String) {
        val intent = Intent(context, NotificationSegmentProgressReceiver::class.java).apply {
            action = NotificationSegmentProgressReceiver.ACTION_PROGRESS_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            progressAlarmRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager?.cancel(pendingIntent)
        Log.i(TAG, "cancelProgressUpdateAlarm: cancelled for session $sessionId")
    }

    private fun completedAlarmRequestCode(sessionId: String) =
        NOTIF_REQUEST_CODE_OFFSET + ALARM_COMPLETED_ID_OFFSET + sessionId.hashCode()

    private fun progressAlarmRequestCode(sessionId: String) =
        NOTIF_REQUEST_CODE_OFFSET + ALARM_PROGRESS_ID_OFFSET + sessionId.hashCode()
}
