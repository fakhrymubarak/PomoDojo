package com.fakhry.pomodojo.features.preferences.ui.mapper

import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.designsystem.model.TimerUi
import com.fakhry.pomodojo.core.utils.primitives.formatDurationMillis
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import kotlinx.collections.immutable.toPersistentList

fun List<TimerSegmentsDomain>.mapToTimelineSegmentsUi(now: Long = 0L, progress: Float = 1f) =
    map { timeline ->
        val remainingMillis = timeline.timer.finishedInMillis - now
        TimelineSegmentUi(
            type = timeline.type.toTypeUi(),
            timer = TimerUi(
                progress = progress,
                durationEpochMs = timeline.timer.durationEpochMs,
                formattedTime = remainingMillis.formatDurationMillis(),
                finishedInMillis = timeline.timer.finishedInMillis,
            ),
            cycleNumber = timeline.cycleNumber,
            timerStatus = timeline.timerStatus.toStatusUi(),
        )
    }.toPersistentList()

private fun TimerStatusDomain.toStatusUi() = when (this) {
    TimerStatusDomain.INITIAL -> TimerStatusUi.INITIAL
    TimerStatusDomain.COMPLETED -> TimerStatusUi.COMPLETED
    TimerStatusDomain.RUNNING -> TimerStatusUi.RUNNING
    TimerStatusDomain.PAUSED -> TimerStatusUi.PAUSED
}

private fun TimerType.toTypeUi() = when (this) {
    TimerType.FOCUS -> TimerTypeUi.FOCUS
    TimerType.SHORT_BREAK -> TimerTypeUi.SHORT_BREAK
    TimerType.LONG_BREAK -> TimerTypeUi.LONG_BREAK
}
