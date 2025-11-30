package com.fakhry.pomodojo.focus.domain.mapper

import android.content.Context
import com.fakhry.pomodojo.core.framework.R
import com.fakhry.pomodojo.core.utils.primitives.formatDurationMillis
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.notification.NotificationSummary
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType

internal fun PomodoroSessionDomain.toNotificationSummary(
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
        quote = quote.withAttribution(),
        isAllSegmentsCompleted = timeline.segments.all {
            it.timerStatus ==
                TimerStatusDomain.COMPLETED
        },
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
