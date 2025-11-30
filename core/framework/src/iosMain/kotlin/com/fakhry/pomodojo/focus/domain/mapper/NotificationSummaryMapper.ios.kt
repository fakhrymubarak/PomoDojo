package com.fakhry.pomodojo.focus.domain.mapper

import com.fakhry.pomodojo.core.utils.primitives.formatDurationMillis
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import com.fakhry.pomodojo.shared.domain.model.focus.NotificationSummary
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain

internal fun PomodoroSessionDomain.toIosNotificationSummary(now: Long): NotificationSummary {
    val currentSegment = timeline.segments.firstOrNull {
        it.timerStatus == TimerStatusDomain.RUNNING || it.timerStatus == TimerStatusDomain.PAUSED
    } ?: timeline.segments.firstOrNull { it.timerStatus != TimerStatusDomain.COMPLETED }

    val cycleLabel = currentSegment?.type?.toLabel() ?: "Focus session in progress"
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
    val timerText = if (isPaused) {
        "$formattedRemaining remaining • Paused"
    } else {
        "$formattedRemaining remaining"
    }

    // Get current cycle and segment name
    val currentCycle = currentSegment?.cycleNumber ?: 1
    val title = "Cycle $currentCycle of $totalCycle • $cycleLabel"

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
        quote = if (!quote.character.isNullOrEmpty() && !quote.sourceTitle.isNullOrEmpty()) {
            "\"${quote.text}\" — ${quote.character}, ${quote.sourceTitle}"
        } else {
            quote.text
        },
        isAllSegmentsCompleted = timeline.segments.all {
            it.timerStatus == TimerStatusDomain.COMPLETED
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

private fun TimerType.toLabel(): String = when (this) {
    TimerType.FOCUS -> "Focus"
    TimerType.SHORT_BREAK -> "Break"
    TimerType.LONG_BREAK -> "Long break"
}
