package com.fakhry.pomodojo.focus.domain

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fakhry.pomodojo.MainActivity
import com.fakhry.pomodojo.R
import com.fakhry.pomodojo.focus.data.db.AndroidAppDependenciesInitializer
import com.fakhry.pomodojo.focus.domain.mapper.toNotificationSummary
import com.fakhry.pomodojo.focus.domain.model.CompletionNotificationSummary
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.ui.mapper.toCompletionSummary
import com.fakhry.pomodojo.utils.orFalse

private const val CHANNEL_ID = "focus_session_channel"
private const val CHANNEL_NAME = "Focus Sessions"
private const val CHANNEL_DESC = "Focus session progress"
private const val REQUEST_CODE_OFFSET = 42_000
private const val PROGRESS_UPDATE_INTERVAL_MS = 10_000L // Update progress every 10 seconds

actual fun provideFocusSessionNotifier(): FocusSessionNotifier {
    val context = AndroidAppDependenciesInitializer.requireContext()
    return AndroidFocusSessionNotifier(context)
}

private const val TAG = "AndroidFocusSessionNotifier"

class AndroidFocusSessionNotifier(private val context: Context) : FocusSessionNotifier {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun schedule(snapshot: PomodoroSessionDomain) {
        ensureChannel()
        val now = System.currentTimeMillis()
        val summary = snapshot.toNotificationSummary(context, now)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(summary.title)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(100, summary.segmentProgressPercent, false)
            .setContentIntent(pendingActivityIntent())
            .setPriority(NotificationCompat.PRIORITY_MAX)

        // Use chronometer for self-updating timer when not paused
        if (!summary.isPaused && summary.finishTimeMillis > 0) {
            builder
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setShowWhen(true)
                .setWhen(summary.finishTimeMillis)
        } else {
            // For paused state, show static time text
            builder.setShowWhen(false)
        }

        builder.setStyle(
            NotificationCompat
                .BigTextStyle()
                .setBigContentTitle(summary.title)
                .bigText(summary.quote),
        )

        notificationManager.notify(sessionNotificationId(summary.sessionId), builder.build())

        // Schedule alarms for segment completion and periodic progress updates
        if (!summary.isPaused && summary.finishTimeMillis > 0) {
            scheduleProgressUpdateAlarm(summary.sessionId, now)
            scheduleSegmentCompletionAlarm(summary.sessionId, summary.finishTimeMillis)
        } else {
            cancelProgressUpdateAlarm(summary.sessionId)
            cancelSegmentCompletionAlarm(summary.sessionId)
        }

        if (summary.isAllSegmentsCompleted) scheduleCompletion(snapshot.toCompletionSummary())
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun scheduleCompletion(summary: CompletionNotificationSummary) {
        notificationManager.cancel(sessionNotificationId(summary.sessionId))
        val title = context.getString(R.string.focus_session_complete_title)
        val body = context.getString(
            R.string.focus_session_complete_body,
            summary.completedCycles,
            summary.totalFocusMinutes,
            summary.totalBreakMinutes,
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(pendingActivityIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText(body),
            )
        notificationManager.notify(completedNotificationId(summary.sessionId), builder.build())
    }

    override suspend fun cancel(sessionId: String) {
        notificationManager.cancel(sessionNotificationId(sessionId))
        cancelSegmentCompletionAlarm(sessionId)
        cancelProgressUpdateAlarm(sessionId)
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
            e.printStackTrace()
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

        // Schedule exact alarm to fire when segment progress updates
        val triggerTime = now + PROGRESS_UPDATE_INTERVAL_MS
        try {
            scheduleExactAlarm(triggerTime, pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule progress update alarm", e)
        }
    }

    @Throws(Exception::class)
    private fun scheduleExactAlarm(triggerAtMillis: Long, operation: PendingIntent) {
        Log.i(TAG, "scheduleExactAlarm at $triggerAtMillis")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager?.canScheduleExactAlarms().orFalse()) {
                alarmManager?.setExactAndAllowWhileIdle(
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

    private fun pendingActivityIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ensureChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannelCompat.Builder(
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_DEFAULT,
            ).setName(CHANNEL_NAME)
                .setDescription(CHANNEL_DESC)
                .setSound(null, null)
                .setVibrationEnabled(false)
                .build()
            notificationManager.createNotificationChannel(channel)
        }
    }
}

private fun sessionNotificationId(sessionId: String) = REQUEST_CODE_OFFSET + sessionId.hashCode()
private fun completedNotificationId(sessionId: String) =
    REQUEST_CODE_OFFSET + 3000 + sessionId.hashCode()

private fun completedAlarmRequestCode(sessionId: String) =
    REQUEST_CODE_OFFSET + 1000 + sessionId.hashCode()

private fun progressAlarmRequestCode(sessionId: String) =
    REQUEST_CODE_OFFSET + 2000 + sessionId.hashCode()
