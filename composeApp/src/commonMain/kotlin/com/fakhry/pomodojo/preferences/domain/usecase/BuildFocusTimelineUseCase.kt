package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineSegmentDomain
import kotlinx.collections.immutable.toPersistentList

class BuildFocusTimelineUseCase {

    operator fun invoke(preferences: PreferencesDomain): List<TimelineSegmentDomain> {
        val segments = mutableListOf<TimelineSegmentDomain>()
        for (cycle in 1..preferences.repeatCount) {
            segments += TimelineSegmentDomain.Focus(preferences.focusMinutes)
            val isLongBreakPoint =
                preferences.longBreakEnabled && cycle % preferences.longBreakAfter == 0
            val isLastFocus = cycle == preferences.repeatCount

            if (!isLastFocus && isLongBreakPoint) {
                segments += TimelineSegmentDomain.LongBreak(preferences.longBreakMinutes)
            } else if (!isLastFocus) {
                segments += TimelineSegmentDomain.ShortBreak(preferences.breakMinutes)
            }
        }

        return segments.toPersistentList()
    }
}
