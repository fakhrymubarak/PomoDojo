package com.fakhry.pomodojo.focus.ui

import com.fakhry.pomodojo.preferences.domain.model.TimerDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimerUi
import com.fakhry.pomodojo.utils.formatDurationMillis

internal fun TimerSegmentsDomain.toTimelineSegmentUi(now: Long): TimelineSegmentUi {
    val duration = timer.durationEpochMs
    val (remaining, finishedAt, startedPauseAt) =
        when (timerStatus) {
            TimerStatusDomain.Running -> {
                val remainingNow = (timer.finishedInMillis - now).coerceAtLeast(0L)
                Triple(
                    remainingNow,
                    timer.finishedInMillis.takeIf {
                        it > 0L
                    } ?: now + duration,
                    0L,
                )
            }

            TimerStatusDomain.Paused -> {
                val remainingNow = (timer.finishedInMillis - timer.startedPauseTime).coerceAtLeast(
                    0L,
                )
                Triple(remainingNow, timer.finishedInMillis, timer.startedPauseTime)
            }

            TimerStatusDomain.Completed -> Triple(0L, timer.finishedInMillis, 0L)
            TimerStatusDomain.Initial -> Triple(duration, timer.finishedInMillis, 0L)
        }
    val progress =
        when (timerStatus) {
            TimerStatusDomain.Completed -> 1f
            TimerStatusDomain.Initial -> 0f
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
    timer =
    TimerDomain(
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
            it.timerStatus == TimerStatusDomain.Running ||
                it.timerStatus == TimerStatusDomain.Paused
        }
    if (runningIndex >= 0) return runningIndex
    return indexOfFirst { it.timerStatus != TimerStatusDomain.Completed }
        .takeUnless { it < 0 } ?: lastIndex.coerceAtLeast(0)
}

internal fun calculateTimerProgress(duration: Long, remaining: Long): Float {
    if (duration <= 0L) return 1f
    val completed = duration - remaining.coerceAtMost(duration)
    return (completed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
}
