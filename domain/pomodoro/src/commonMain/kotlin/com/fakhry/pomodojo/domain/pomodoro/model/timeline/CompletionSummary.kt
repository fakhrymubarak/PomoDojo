package com.fakhry.pomodojo.domain.pomodoro.model.timeline

import kotlin.math.roundToLong

private const val MILLIS_PER_MINUTE = 60_000L

data class CompletionSummary(val focusMinutes: Int, val breakMinutes: Int, val cyclesFinished: Int)

fun TimelineDomain.completionSummary(): CompletionSummary {
    val completed = segments.filter { it.timerStatus == TimerStatusDomain.COMPLETED }
    val (focus, breaks) = completed.partition { it.type == TimerType.FOCUS }
    return CompletionSummary(
        focusMinutes = focus.creditedMinutes(),
        breakMinutes = breaks.creditedMinutes(),
        cyclesFinished = focus.count { it.timer.progress >= 1f },
    )
}

private fun List<TimerSegmentsDomain>.creditedMinutes(): Int =
    (sumOf { (it.timer.durationEpochMs * it.timer.progress).roundToLong() } / MILLIS_PER_MINUTE)
        .toInt()
