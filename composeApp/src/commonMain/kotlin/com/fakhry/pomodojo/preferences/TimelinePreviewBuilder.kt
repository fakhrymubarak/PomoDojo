package com.fakhry.pomodojo.preferences

class TimelinePreviewBuilder {

    fun build(preferences: PomodoroPreferences): List<TimelineSegment> {
        val segments = mutableListOf<TimelineSegment>()

        for (cycle in 1..preferences.repeatCount) {
            segments += TimelineSegment.Focus(preferences.focusMinutes)

            val isLongBreakPoint = preferences.longBreakEnabled &&
                cycle % preferences.longBreakAfter == 0

            val isLastFocus = cycle == preferences.repeatCount

            if (isLongBreakPoint) {
                segments += TimelineSegment.LongBreak(preferences.longBreakMinutes)
            } else if (!isLastFocus) {
                segments += TimelineSegment.ShortBreak(preferences.breakMinutes)
            }
        }

        return segments
    }
}
