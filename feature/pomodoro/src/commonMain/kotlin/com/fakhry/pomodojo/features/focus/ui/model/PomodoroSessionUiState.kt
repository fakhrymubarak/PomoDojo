package com.fakhry.pomodojo.features.focus.ui.model

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent

@Immutable
data class PomodoroSessionUiState(
    val totalCycle: Int = 0,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val activeSegment: TimelineSegmentUi = TimelineSegmentUi(),
    val timeline: TimelineUiModel = TimelineUiModel(),
    val quote: QuoteContent = QuoteContent.DEFAULT_QUOTE,
    val isShowConfirmEndDialog: Boolean = false,
    val isComplete: Boolean = false,
)

sealed class PomodoroSessionSideEffect {
    data class ShowEndSessionDialog(val isShown: Boolean) : PomodoroSessionSideEffect()
    data class OnSessionComplete(
        val completionResult: PomodoroCompletionUiState,
    ) : PomodoroSessionSideEffect()
}
