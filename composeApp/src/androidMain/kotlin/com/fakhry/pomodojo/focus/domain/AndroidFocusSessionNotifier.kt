package com.fakhry.pomodojo.focus.domain

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fakhry.pomodojo.MainActivity
import com.fakhry.pomodojo.R
import com.fakhry.pomodojo.focus.data.db.AndroidFocusDatabaseHolder
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

actual fun provideFocusSessionNotifier(): FocusSessionNotifier {
    val context = AndroidFocusDatabaseHolder.requireContext()
    return AndroidFocusSessionNotifier(context)
}

class AndroidFocusSessionNotifier(private val context: Context) : FocusSessionNotifier {
    private val notificationManager = NotificationManagerCompat.from(context)

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun schedule(snapshot: PomodoroSessionDomain) {
        ensureChannel()
        val summary = snapshot.toNotificationSummary(context, System.currentTimeMillis())
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(summary.title)
            .setContentText("")
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setSilent(true)
            .setProgress(100, summary.segmentProgressPercent, false)
            .setContentIntent(buildContentIntent())
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .setBigContentTitle(summary.title)
                    .setSummaryText(summary.body)
                    .bigText(summary.timerText),
            ).build()
        notificationManager.notify(sessionNotificationId(summary.sessionId), notification)
    }

    override suspend fun cancel(sessionId: String) {
        notificationManager.cancel(sessionNotificationId(sessionId))
    }

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
    val body: String,
    val segmentProgressPercent: Int,
    val isPaused: Boolean,
)

private fun PomodoroSessionDomain.toNotificationSummary(
    context: Context,
    now: Long,
): NotificationSummary {
    val currentSegment = timeline.segments.firstOrNull {
        it.timerStatus == TimerStatusDomain.Running || it.timerStatus == TimerStatusDomain.Paused
    } ?: timeline.segments.firstOrNull { it.timerStatus != TimerStatusDomain.Completed }
    val segmentLabel = currentSegment?.type?.toLabel(context)
        ?: context.getString(R.string.focus_session_live_title)
    val remaining = currentSegment?.let { segmentRemaining(it, now) } ?: 0L
    val segmentDuration = currentSegment?.timer?.durationEpochMs ?: 0L
    val segmentProgress = if (segmentDuration <= 0L) {
        0
    } else {
        val elapsed = (segmentDuration - remaining).coerceAtLeast(0L)
        ((elapsed * 100) / segmentDuration).toInt().coerceIn(0, 100)
    }

    val timerText = context.getString(
        R.string.focus_session_notification_subtitle_format,
        remaining.formatDurationMillis(),
    )
    val segmentIndex = timeline.segments.count { it.timerStatus == TimerStatusDomain.Completed } + 1
    val body = context.getString(
        R.string.focus_session_notification_body_format,
        segmentIndex.coerceAtMost(timeline.segments.size),
        timeline.segments.size,
    )
    return NotificationSummary(
        sessionId = sessionId(),
        title = context.getString(
            R.string.focus_session_notification_title_format,
            segmentLabel,
        ),
        timerText = timerText,
        body = body,
        segmentProgressPercent = segmentProgress,
        isPaused = currentSegment?.timerStatus == TimerStatusDomain.Paused,
    )
}

private fun segmentRemaining(segment: TimerSegmentsDomain, now: Long): Long =
    when (segment.timerStatus) {
        TimerStatusDomain.Completed -> 0L
        TimerStatusDomain.Initial -> segment.timer.durationEpochMs
        TimerStatusDomain.Running -> (segment.timer.finishedInMillis - now).coerceAtLeast(0L)
        TimerStatusDomain.Paused -> {
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
