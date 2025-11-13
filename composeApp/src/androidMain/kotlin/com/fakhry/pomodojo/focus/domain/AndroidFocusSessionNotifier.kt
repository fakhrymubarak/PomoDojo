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
import com.fakhry.pomodojo.focus.data.db.AndroidAppInitializer
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.model.sessionId
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.utils.formatDurationMillis

private const val CHANNEL_ID = "focus_session_channel"
private const val CHANNEL_NAME = "Focus Sessions"
private const val REQUEST_CODE_OFFSET = 42_000
private const val PROGRESS_UPDATE_INTERVAL_MS = 10_000L // Update progress every 10 seconds

actual fun provideFocusSessionNotifier(): FocusSessionNotifier {
    val context = AndroidAppInitializer.requireContext()
    return AndroidFocusSessionNotifier(context)
}

private const val TAG = "AndroidFocusSessionNotifier"

class AndroidFocusSessionNotifier(private val context: Context) : FocusSessionNotifier {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
            .setContentIntent(buildContentIntent())
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
            builder
                .setShowWhen(false)
                .setSubText(summary.timerText)
        }

        notificationManager.notify(sessionNotificationId(summary.sessionId), builder.build())

        // Schedule alarms for segment completion and periodic progress updates
        if (!summary.isPaused && summary.finishTimeMillis > 0) {
            scheduleSegmentCompletionAlarm(summary.sessionId, summary.finishTimeMillis)
            scheduleProgressUpdateAlarm(summary.sessionId)
        } else {
            cancelSegmentCompletionAlarm(summary.sessionId)
            cancelProgressUpdateAlarm(summary.sessionId)
        }
    }

    override suspend fun cancel(sessionId: String) {
        notificationManager.cancel(sessionNotificationId(sessionId))
        cancelSegmentCompletionAlarm(sessionId)
        cancelProgressUpdateAlarm(sessionId)
    }

    private fun scheduleSegmentCompletionAlarm(sessionId: String, finishTime: Long) {
        val intent = Intent(context, SegmentCompletionReceiver::class.java).apply {
            action = SegmentCompletionReceiver.ACTION_SEGMENT_COMPLETE
            putExtra(SegmentCompletionReceiver.EXTRA_SESSION_ID, sessionId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Schedule exact alarm to fire when segment completes
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        finishTime,
                        pendingIntent,
                    )
                } else {
                    // Fallback to inexact alarm if exact alarms not permitted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        finishTime,
                        pendingIntent,
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    finishTime,
                    pendingIntent,
                )
            }
        } catch (e: Exception) {
            // Silently fail - notification will still update via ViewModel
            Log.e(TAG, e.message ?: "Failed to schedule segment completion alarm")
            e.printStackTrace()
        }
    }

    private fun cancelSegmentCompletionAlarm(sessionId: String) {
        val intent = Intent(context, SegmentCompletionReceiver::class.java).apply {
            action = SegmentCompletionReceiver.ACTION_SEGMENT_COMPLETE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleProgressUpdateAlarm(sessionId: String) {
        val intent = Intent(context, SegmentCompletionReceiver::class.java).apply {
            action = SegmentCompletionReceiver.ACTION_PROGRESS_UPDATE
            putExtra(SegmentCompletionReceiver.EXTRA_SESSION_ID, sessionId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            progressAlarmRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Schedule inexact repeating alarm for battery efficiency
        // Android can batch these with other alarms
        val triggerTime = System.currentTimeMillis() + PROGRESS_UPDATE_INTERVAL_MS
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent,
                    )
                } else {
                    // Fallback to inexact alarm if exact alarms not permitted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent,
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent,
                )
            }
            Log.i(TAG, "scheduleProgressUpdateAlarm: scheduled for session $sessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule progress update alarm", e)
        }
    }

    private fun cancelProgressUpdateAlarm(sessionId: String) {
        val intent = Intent(context, SegmentCompletionReceiver::class.java).apply {
            action = SegmentCompletionReceiver.ACTION_PROGRESS_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            progressAlarmRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager.cancel(pendingIntent)
        Log.i(TAG, "cancelProgressUpdateAlarm: cancelled for session $sessionId")
    }

    private fun alarmRequestCode(sessionId: String) =
        REQUEST_CODE_OFFSET + 1000 + sessionId.hashCode()

    private fun progressAlarmRequestCode(sessionId: String) =
        REQUEST_CODE_OFFSET + 2000 + sessionId.hashCode()

    private fun buildContentIntent(): PendingIntent = pendingActivityIntent()

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
                NotificationManagerCompat.IMPORTANCE_LOW,
            ).setName(CHANNEL_NAME).setDescription("Focus session progress").setSound(null, null)
                .setVibrationEnabled(false).build()
            notificationManager.createNotificationChannel(channel)
        }
    }
}

private data class NotificationSummary(
    val sessionId: String,
    val title: String,
    val timerText: String,
    val segmentProgressPercent: Int,
    val isPaused: Boolean,
    val finishTimeMillis: Long,
)

private fun PomodoroSessionDomain.toNotificationSummary(
    context: Context,
    now: Long,
): NotificationSummary {
    val currentSegment = timeline.segments.firstOrNull {
        it.timerStatus == TimerStatusDomain.RUNNING || it.timerStatus == TimerStatusDomain.PAUSED
    } ?: timeline.segments.firstOrNull { it.timerStatus != TimerStatusDomain.COMPLETED }
    val cycleLabel = currentSegment?.type?.toLabel(context)
        ?: context.getString(R.string.focus_session_live_title)
    val remaining = currentSegment?.let { segmentRemaining(it, now) } ?: 0L

    // Calculate progress dynamically based on remaining time for accurate updates in background
    val segmentDuration = currentSegment?.timer?.durationEpochMs ?: 0L
    val segmentProgress = if (segmentDuration > 0L) {
        val elapsed = (segmentDuration - remaining).coerceAtLeast(0L)
        ((elapsed * 100) / segmentDuration).toInt().coerceIn(0, 100)
    } else {
        0
    }

    val isPaused = currentSegment?.timerStatus == TimerStatusDomain.PAUSED
    val formattedRemaining = remaining.formatDurationMillis()
    val resId = if (isPaused) {
        R.string.focus_session_notification_subtitle_format_paused
    } else {
        R.string.focus_session_notification_subtitle_format_running
    }
    val timerText = context.getString(resId, formattedRemaining)

    // Get current cycle and segment name
    val currentCycle = currentSegment?.cycleNumber ?: 1
    val title = context.getString(
        R.string.focus_session_notification_body_format,
        currentCycle,
        totalCycle,
        cycleLabel,
    )

    // Calculate finish time for chronometer (when the segment will complete)
    val finishTime = if (currentSegment?.timerStatus == TimerStatusDomain.RUNNING) {
        currentSegment.timer.finishedInMillis
    } else {
        0L
    }

    return NotificationSummary(
        sessionId = sessionId(),
        title = title,
        timerText = timerText,
        segmentProgressPercent = segmentProgress,
        isPaused = isPaused,
        finishTimeMillis = finishTime,
    )
}

private fun segmentRemaining(segment: TimerSegmentsDomain, now: Long): Long =
    when (segment.timerStatus) {
        TimerStatusDomain.COMPLETED -> 0L
        TimerStatusDomain.INITIAL -> segment.timer.durationEpochMs
        TimerStatusDomain.RUNNING -> (segment.timer.finishedInMillis - now).coerceAtLeast(0L)
        TimerStatusDomain.PAUSED -> {
            val remaining = segment.timer.finishedInMillis - segment.timer.startedPauseTime
            remaining.coerceAtLeast(0L)
        }
    }

private fun TimerType.toLabel(context: Context): String = when (this) {
    TimerType.FOCUS -> context.getString(R.string.focus_session_phase_focus_label)
    TimerType.SHORT_BREAK -> context.getString(R.string.focus_session_phase_short_break_label)
    TimerType.LONG_BREAK -> context.getString(R.string.focus_session_phase_long_break_label)
}

private fun sessionNotificationId(sessionId: String) = REQUEST_CODE_OFFSET + sessionId.hashCode()
