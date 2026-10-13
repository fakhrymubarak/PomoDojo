package com.fakhry.pomodojo.features.focus.ui.mapper

import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.CompletionSummary
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelinePhase
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.elapsedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.phaseAt
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroCompletionUiState
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import kotlinx.collections.immutable.toPersistentList

/** Projects the Session as it stands at [now]; at the Gate the active Segment is the finished one. */
fun PomodoroSessionDomain.toUiState(
    now: Long,
    isComplete: Boolean = false,
): PomodoroSessionUiState {
    val elapsed = timeline.elapsedAt(now)
    val segments = elapsed.segments.map { it.toTimelineSegmentUi(now) }
    val phase = elapsed.phaseAt(now)
    val activeIndex = when (phase) {
        is TimelinePhase.Running -> phase.index
        is TimelinePhase.Paused -> phase.index
        is TimelinePhase.AtGate -> phase.finishedIndex
        TimelinePhase.Complete -> segments.lastIndex
        TimelinePhase.NotStarted -> 0
    }
    val next = segments.getOrNull(activeIndex + 1)
        .takeIf { phase is TimelinePhase.AtGate && !isComplete }
    return PomodoroSessionUiState(
        totalCycle = totalCycle,
        startedAtEpochMs = startedAtEpochMs,
        elapsedPauseEpochMs = elapsedPauseEpochMs,
        activeSegment = segments.getOrNull(activeIndex) ?: TimelineSegmentUi(),
        timeline = TimelineUiModel(
            segments = segments.toPersistentList(),
            hourSplits = timeline.hourSplits.toPersistentList(),
        ),
        quote = quote,
        isComplete = isComplete,
        awaitingContinue = next != null,
        finishedPhaseType = next?.let { segments[activeIndex].type },
        nextPhaseType = next?.type,
        nextPhaseDurationMs = next?.timer?.durationEpochMs ?: 0L,
    )
}

fun CompletionSummary.toUiState() = PomodoroCompletionUiState(
    totalCyclesFinished = cyclesFinished,
    totalFocusMinutes = focusMinutes,
    totalBreakMinutes = breakMinutes,
)
