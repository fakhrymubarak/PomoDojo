package com.fakhry.pomodojo.preferences.domain

import com.fakhry.pomodojo.preferences.ui.model.TimelineSegment
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

class TimelinePreviewBuilder {

    fun build(preferences: PomodoroPreferences): ImmutableList<TimelineSegment> {
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

        return segments.toPersistentList()
    }
}