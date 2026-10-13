package com.fakhry.pomodojo.features.focus.ui.mapper

import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.designsystem.model.TimerUi
import com.fakhry.pomodojo.core.utils.primitives.formatDurationMillis
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.progressAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.remainingAt

internal fun TimerSegmentsDomain.toTimelineSegmentUi(now: Long): TimelineSegmentUi =
    TimelineSegmentUi(
        type = type.toTypeUi(),
        cycleNumber = cycleNumber,
        timer = TimerUi(
            progress = progressAt(now),
            durationEpochMs = timer.durationEpochMs,
            finishedInMillis = timer.finishedInMillis,
            formattedTime = remainingAt(now).formatDurationMillis(),
            startedPauseTime = timer.startedPauseTime,
        ),
        timerStatus = timerStatus.toStatusUi(),
    )

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
