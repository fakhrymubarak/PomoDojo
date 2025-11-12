package com.fakhry.pomodojo.focus.ui.mapper

import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.ui.PomodoroSessionUiState
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
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
