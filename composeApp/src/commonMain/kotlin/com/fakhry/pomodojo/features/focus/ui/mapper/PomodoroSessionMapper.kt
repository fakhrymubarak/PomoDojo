package com.fakhry.pomodojo.features.focus.ui.mapper

import com.fakhry.pomodojo.core.utils.primitives.toMinutes
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroCompletionUiState
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import com.fakhry.pomodojo.features.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.features.preferences.ui.model.TimelineUiModel
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerType
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
        it.timerStatus == TimerStatusDomain.INITIAL
    }
    val totalFocusMinutes = elapsedSegments
        .filter { it.type == TimerType.FOCUS }
        .sumOf { (it.timer.durationEpochMs * it.timer.progress).toLong() }
        .toMinutes()
    val totalBreakMinutes = elapsedSegments
        .filter { it.type != TimerType.FOCUS }
        .sumOf { (it.timer.durationEpochMs * it.timer.progress).toLong() }
        .toMinutes()

    val totalCycleFinished = elapsedSegments
        .filter { it.type != TimerType.FOCUS }
        .count { it.timerStatus == TimerStatusDomain.COMPLETED }

    return PomodoroCompletionUiState(
        totalCyclesFinished = totalCycleFinished,
        totalFocusMinutes = totalFocusMinutes,
        totalBreakMinutes = totalBreakMinutes,
    )
}
