package com.fakhry.pomodojo.domain.pomodoro.model.timeline

import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60

/**
 * The Timeline plan for [preferences]: every Cycle is a focus Segment and a break, all unstarted.
 * Values the UI cannot produce are tolerated: `longBreakAfter <= 0` means no long breaks and
 * `repeatCount < 1` plans an empty Timeline.
 */
fun planTimeline(preferences: PomodoroPreferences): TimelineDomain = with(preferences) {
    val segments = (1..repeatCount).flatMap { cycle ->
        val isLongBreak = longBreakEnabled && longBreakAfter > 0 && cycle % longBreakAfter == 0
        listOf(
            segment(TimerType.FOCUS, cycle, focusMinutes),
            if (isLongBreak) {
                segment(TimerType.LONG_BREAK, cycle, longBreakMinutes)
            } else {
                segment(TimerType.SHORT_BREAK, cycle, breakMinutes)
            },
        )
    }
    TimelineDomain(segments = segments, hourSplits = segments.hourSplits())
}

private fun segment(type: TimerType, cycle: Int, minutes: Int) = TimerSegmentsDomain(
    type = type,
    cycleNumber = cycle,
    timer = TimerDomain(durationEpochMs = minutes * MILLIS_PER_MINUTE),
)

/** Total minutes as full hours of 60 plus the remainder, if any. */
private fun List<TimerSegmentsDomain>.hourSplits(): List<Int> {
    val totalMinutes = (sumOf { it.timer.durationEpochMs } / MILLIS_PER_MINUTE).toInt()
    val remainder = totalMinutes % MINUTES_PER_HOUR
    return List(totalMinutes / MINUTES_PER_HOUR) { MINUTES_PER_HOUR } +
        listOfNotNull(remainder.takeIf { it != 0 })
}
