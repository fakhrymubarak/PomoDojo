package com.fakhry.pomodojo.preferences.ui.mapper

import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import com.fakhry.pomodojo.preferences.ui.model.TimerUi
import com.fakhry.pomodojo.utils.formatDurationMillis
import kotlinx.collections.immutable.toPersistentList

fun TimelineDomain.toTimelineUi(now: Long = 0L, progress: Float = 1f) = TimelineUiModel(
    segments = segments.mapToTimelineSegmentsUi(now, progress),
    hourSplits = hourSplits.toPersistentList(),
)

fun List<TimerSegmentsDomain>.mapToTimelineSegmentsUi(now: Long = 0L, progress: Float = 1f) =
    map { timeline ->
        val remainingMillis = timeline.timer.finishedInMillis - now
        TimelineSegmentUi(
            type = timeline.type,
            timer =
            TimerUi(
                progress = progress,
                durationEpochMs = timeline.timer.durationEpochMs,
                formattedTime = remainingMillis.formatDurationMillis(),
                finishedInMillis = timeline.timer.finishedInMillis,
            ),
            cycleNumber = timeline.cycleNumber,
            timerStatus = timeline.timerStatus,
        )
    }.toPersistentList()
