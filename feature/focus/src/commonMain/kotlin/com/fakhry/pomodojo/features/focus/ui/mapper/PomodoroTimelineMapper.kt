package com.fakhry.pomodojo.features.focus.ui.mapper

import com.fakhry.pomodojo.core.utils.primitives.formatDurationMillis
import com.fakhry.pomodojo.features.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.features.preferences.ui.model.TimerUi
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerStatusDomain

internal fun TimerSegmentsDomain.toTimelineSegmentUi(now: Long): TimelineSegmentUi {
    val duration = timer.durationEpochMs
    val (remaining, finishedAt, startedPauseAt) =
        when (timerStatus) {
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
    val progress =
        when (timerStatus) {
            TimerStatusDomain.COMPLETED -> 1f
            TimerStatusDomain.INITIAL -> 0f
            else -> calculateTimerProgress(duration, remaining)
        }
    val timerUi =
        TimerUi(
            progress = progress,
            durationEpochMs = duration,
            finishedInMillis = finishedAt,
            formattedTime = remaining.formatDurationMillis(),
            startedPauseTime = startedPauseAt,
            elapsedPauseTime = timer.elapsedPauseTime,
        )
    return TimelineSegmentUi(
        type = type,
        cycleNumber = cycleNumber,
        timer = timerUi,
        timerStatus = timerStatus,
    )
}

internal fun TimelineSegmentUi.toDomainSegment(): TimerSegmentsDomain = TimerSegmentsDomain(
    type = type,
    cycleNumber = cycleNumber,
    timer = TimerDomain(
        progress = timer.progress,
        durationEpochMs = timer.durationEpochMs,
        finishedInMillis = timer.finishedInMillis,
        startedPauseTime = timer.startedPauseTime,
        elapsedPauseTime = timer.elapsedPauseTime,
    ),
    timerStatus = timerStatus,
)

internal fun List<TimelineSegmentUi>.resolveActiveIndex(): Int {
    if (isEmpty()) return 0
    val runningIndex =
        indexOfFirst {
            it.timerStatus == TimerStatusDomain.RUNNING ||
                it.timerStatus == TimerStatusDomain.PAUSED
        }
    if (runningIndex >= 0) return runningIndex
    return indexOfFirst { it.timerStatus != TimerStatusDomain.COMPLETED }
        .takeUnless { it < 0 } ?: lastIndex.coerceAtLeast(0)
}

internal fun calculateTimerProgress(duration: Long, remaining: Long): Float {
    if (duration <= 0L) return 1f
    val completed = duration - remaining.coerceAtMost(duration)
    return (completed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
}
