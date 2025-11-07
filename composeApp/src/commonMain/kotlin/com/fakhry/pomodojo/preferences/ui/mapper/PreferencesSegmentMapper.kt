package com.fakhry.pomodojo.preferences.ui.mapper

import com.fakhry.pomodojo.preferences.domain.model.TimelineSegmentDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import kotlinx.collections.immutable.toPersistentList

fun List<TimelineSegmentDomain>.mapToTimelineSegmentsUi() = map {
    when (it) {
        is TimelineSegmentDomain.Focus -> TimelineSegmentUiModel.Focus(it.duration)
        is TimelineSegmentDomain.ShortBreak -> TimelineSegmentUiModel.ShortBreak(it.duration)
        is TimelineSegmentDomain.LongBreak -> TimelineSegmentUiModel.LongBreak(it.duration)
    }
}.toPersistentList()
