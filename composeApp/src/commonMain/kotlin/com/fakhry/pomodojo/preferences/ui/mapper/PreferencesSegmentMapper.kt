package com.fakhry.pomodojo.preferences.ui.mapper

import com.fakhry.pomodojo.preferences.domain.model.TimelineSegmentDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import kotlinx.collections.immutable.toPersistentList

fun List<TimelineSegmentDomain>.mapToTimelineSegmentsUi() = map {
    when (it) {
        is TimelineSegmentDomain.Focus -> TimelineSegmentUiModel.Focus(it.duration, 1f)
        is TimelineSegmentDomain.ShortBreak -> TimelineSegmentUiModel.ShortBreak(it.duration, 1f)
        is TimelineSegmentDomain.LongBreak -> TimelineSegmentUiModel.LongBreak(it.duration, 1f)
    }
}.toPersistentList()
