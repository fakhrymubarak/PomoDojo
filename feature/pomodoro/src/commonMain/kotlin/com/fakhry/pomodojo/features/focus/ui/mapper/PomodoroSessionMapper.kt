package com.fakhry.pomodojo.features.focus.ui.mapper

import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.utils.primitives.toMinutes
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroCompletionUiState
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import kotlinx.collections.immutable.toPersistentList

fun PomodoroSessionDomain.toUiState(
    segments: List<TimelineSegmentUi>,
    activeIndex: Int,
    isComplete: Boolean,
): PomodoroSessionUiState {
    val active = segments.getOrNull(activeIndex) ?: TimelineSegmentUi()
    return PomodoroSessionUiState(
        totalCycle = totalCycle,
        startedAtEpochMs = startedAtEpochMs,
        elapsedPauseEpochMs = elapsedPauseEpochMs,
        activeSegment = active,
        timeline = TimelineUiModel(
            segments = segments.toPersistentList(),
            hourSplits = timeline.hourSplits.toPersistentList(),
        ),
        quote = quote,
        isShowConfirmEndDialog = false,
        isComplete = isComplete,
    )
}

fun PomodoroSessionUiState.toCompletionSummary(): PomodoroCompletionUiState {
    val elapsedSegments = timeline.segments.filterNot {
        it.timerStatus == TimerStatusUi.INITIAL
    }
    val totalFocusMinutes = elapsedSegments
        .filter { it.type == TimerTypeUi.FOCUS }
        .sumOf { (it.timer.durationEpochMs * it.timer.progress).toLong() }
        .toMinutes()
    val totalBreakMinutes = elapsedSegments
        .filter { it.type != TimerTypeUi.FOCUS }
        .sumOf { (it.timer.durationEpochMs * it.timer.progress).toLong() }
        .toMinutes()

    val totalCycleFinished = elapsedSegments
        .filter { it.type != TimerTypeUi.FOCUS }
        .count { it.timerStatus == TimerStatusUi.COMPLETED }

    return PomodoroCompletionUiState(
        totalCyclesFinished = totalCycleFinished,
        totalFocusMinutes = totalFocusMinutes,
        totalBreakMinutes = totalBreakMinutes,
    )
}
