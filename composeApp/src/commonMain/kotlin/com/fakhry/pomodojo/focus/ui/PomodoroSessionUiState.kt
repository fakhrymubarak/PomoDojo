package com.fakhry.pomodojo.focus.ui

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.ui.mapper.toTimelineUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel

@Immutable
data class PomodoroSessionUiState(
    val totalCycle: Int = 0,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val timeline: TimelineUiModel = TimelineUiModel(),
    val quote: QuoteContent = QuoteContent.DEFAULT_QUOTE,
    val isShowConfirmEndDialog: Boolean = false,
    val isComplete: Boolean = false,
) {
    val activeSegment: TimelineSegmentUi
        get() = timeline.segments.firstOrNull {
            it.timerStatus !is TimerStatusDomain.Initial && it.timerStatus !is TimerStatusDomain.Completed
        } ?: TimelineSegmentUi()
}

sealed class PomodoroSessionSideEffect {
    data class ShowEndSessionDialog(val isShown: Boolean) : PomodoroSessionSideEffect()
    object OnSessionComplete : PomodoroSessionSideEffect()
}

fun PomodoroSessionDomain.toPomodoroUiSessionUi(): PomodoroSessionUiState {
    return PomodoroSessionUiState(
        totalCycle = totalCycle,
        startedAtEpochMs = startedAtEpochMs,
        elapsedPauseEpochMs = elapsedPauseEpochMs,
        timeline = timeline.toTimelineUi(),
        quote = quote,
        isShowConfirmEndDialog = false,
        isComplete = false,
    )
}