package com.fakhry.pomodojo.preferences.ui.mapper

import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineTimerDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import kotlinx.collections.immutable.toPersistentList


fun TimelineDomain.toTimelineUi() = TimelineUiModel(
    segments = segments.mapToTimelineSegmentsUi(),
    hourSplits = hourSplits.toPersistentList()
)

fun List<TimelineTimerDomain>.mapToTimelineSegmentsUi() = mapIndexed { index, timeline ->
    index == 0
    TimelineSegmentUi(
        type = timeline.type,
        cycleNumber = timeline.cycleNumber,
        timerStatus = timeline.timerStatus
    )
}.toPersistentList()
