package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

class BuildFocusTimelineUseCase {

    operator fun invoke(preferences: PreferencesDomain): ImmutableList<TimelineSegmentUiModel> {
        val segments = mutableListOf<TimelineSegmentUiModel>()
        var totalMinutes = 0

        // Calculate Total Minutes
        for (cycle in 1..preferences.repeatCount) {
            totalMinutes += preferences.focusMinutes

            val isLongBreakPoint =
                preferences.longBreakEnabled && cycle % preferences.longBreakAfter == 0
            val isLastFocus = cycle == preferences.repeatCount

            // Avoid addition break on last focus
            if (!isLastFocus && isLongBreakPoint) {
                totalMinutes += preferences.longBreakMinutes
            } else if (!isLastFocus) {
                totalMinutes += preferences.breakMinutes
            }
        }

        for (cycle in 1..preferences.repeatCount) {
            val weight = preferences.focusMinutes / totalMinutes.toFloat()
            segments += TimelineSegmentUiModel.Focus(preferences.focusMinutes, weight)
            val isLongBreakPoint =
                preferences.longBreakEnabled && cycle % preferences.longBreakAfter == 0
            val isLastFocus = cycle == preferences.repeatCount

            // Avoid addition break on last focus
            if (!isLastFocus && isLongBreakPoint) {
                val weight = preferences.longBreakMinutes / totalMinutes.toFloat()
                segments += TimelineSegmentUiModel.LongBreak(preferences.longBreakMinutes, weight)
                totalMinutes += preferences.longBreakMinutes
            } else if (!isLastFocus) {
                val weight = preferences.breakMinutes / totalMinutes.toFloat()
                segments += TimelineSegmentUiModel.ShortBreak(preferences.breakMinutes, weight)
                totalMinutes += preferences.breakMinutes
            }
        }

        return segments.toPersistentList()
    }
}