package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

class BuildFocusTimelineUseCase {

    operator fun invoke(preferences: PreferencesDomain): ImmutableList<TimelineSegmentUiModel> {
        val segments = mutableListOf<TimelineSegmentUiModel>()
        for (cycle in 1..preferences.repeatCount) {
            segments += TimelineSegmentUiModel.Focus(preferences.focusMinutes)
            val isLongBreakPoint =
                preferences.longBreakEnabled && cycle % preferences.longBreakAfter == 0
            val isLastFocus = cycle == preferences.repeatCount

            if (!isLastFocus && isLongBreakPoint) {
                segments += TimelineSegmentUiModel.LongBreak(preferences.longBreakMinutes)
            } else if (!isLastFocus) {
                segments += TimelineSegmentUiModel.ShortBreak(preferences.breakMinutes)
            }
        }

        return segments.toPersistentList()
    }
}