package com.fakhry.pomodojo.features.focus.ui.mapper

import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.designsystem.model.TimerUi
import com.fakhry.pomodojo.core.utils.primitives.formatDurationMillis
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType

internal fun TimerSegmentsDomain.toTimelineSegmentUi(now: Long): TimelineSegmentUi {
    val duration = timer.durationEpochMs
    val (remaining, finishedAt, startedPauseAt) = when (timerStatus) {
        TimerStatusDomain.RUNNING -> {
            val remainingNow = (timer.finishedInMillis - now).coerceAtLeast(0L)
            Triple(
                remainingNow,
                timer.finishedInMillis.takeIf {
                    it > 0L
                } ?: (now + duration),
                0L,
            )
        }

        TimerStatusDomain.PAUSED -> {
            val remainingNow = (timer.finishedInMillis - timer.startedPauseTime).coerceAtLeast(
                0L,
            )
            Triple(remainingNow, timer.finishedInMillis, timer.startedPauseTime)
        }

        TimerStatusDomain.COMPLETED -> Triple(0L, timer.finishedInMillis, 0L)
        TimerStatusDomain.INITIAL -> Triple(duration, timer.finishedInMillis, 0L)
    }
    val progress = when (timerStatus) {
        TimerStatusDomain.COMPLETED -> 1f
        TimerStatusDomain.INITIAL -> 0f
        else -> calculateTimerProgress(duration, remaining)
    }
    val timerUi = TimerUi(
        progress = progress,
        durationEpochMs = duration,
        finishedInMillis = finishedAt,
        formattedTime = remaining.formatDurationMillis(),
        startedPauseTime = startedPauseAt,
        elapsedPauseTime = timer.elapsedPauseTime,
    )
    return TimelineSegmentUi(
        type = type.toTypeUi(),
        cycleNumber = cycleNumber,
        timer = timerUi,
        timerStatus = timerStatus.toStatusUi(),
    )
}

internal fun TimelineSegmentUi.toDomainSegment(): TimerSegmentsDomain = TimerSegmentsDomain(
    type = type.toTypeDomain(),
    cycleNumber = cycleNumber,
    timer = TimerDomain(
        progress = timer.progress,
        durationEpochMs = timer.durationEpochMs,
        finishedInMillis = timer.finishedInMillis,
        startedPauseTime = timer.startedPauseTime,
        elapsedPauseTime = timer.elapsedPauseTime,
    ),
    timerStatus = timerStatus.toStatusDomain(),
)

internal fun List<TimelineSegmentUi>.resolveActiveIndex(): Int {
    if (isEmpty()) return 0
    val runningIndex = indexOfFirst {
        it.timerStatus == TimerStatusUi.RUNNING || it.timerStatus == TimerStatusUi.PAUSED
    }
    if (runningIndex >= 0) return runningIndex
    return indexOfFirst { it.timerStatus != TimerStatusUi.COMPLETED }.takeUnless { it < 0 }
        ?: lastIndex.coerceAtLeast(0)
}

internal fun calculateTimerProgress(duration: Long, remaining: Long): Float {
    if (duration <= 0L) return 1f
    val completed = duration - remaining.coerceAtMost(duration)
    return (completed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
}

private fun TimerStatusDomain.toStatusUi() = when (this) {
    TimerStatusDomain.INITIAL -> TimerStatusUi.INITIAL
    TimerStatusDomain.COMPLETED -> TimerStatusUi.COMPLETED
    TimerStatusDomain.RUNNING -> TimerStatusUi.RUNNING
    TimerStatusDomain.PAUSED -> TimerStatusUi.PAUSED
}

private fun TimerType.toTypeUi() = when (this) {
    TimerType.FOCUS -> TimerTypeUi.FOCUS
    TimerType.SHORT_BREAK -> TimerTypeUi.SHORT_BREAK
    TimerType.LONG_BREAK -> TimerTypeUi.LONG_BREAK
}


private fun TimerStatusUi.toStatusDomain() = when (this) {
    TimerStatusUi.INITIAL -> TimerStatusDomain.INITIAL
    TimerStatusUi.COMPLETED -> TimerStatusDomain.COMPLETED
    TimerStatusUi.RUNNING -> TimerStatusDomain.RUNNING
    TimerStatusUi.PAUSED -> TimerStatusDomain.PAUSED
}

private fun TimerTypeUi.toTypeDomain() = when (this) {
    TimerTypeUi.FOCUS -> TimerType.FOCUS
    TimerTypeUi.SHORT_BREAK -> TimerType.SHORT_BREAK
    TimerTypeUi.LONG_BREAK -> TimerType.LONG_BREAK
}


