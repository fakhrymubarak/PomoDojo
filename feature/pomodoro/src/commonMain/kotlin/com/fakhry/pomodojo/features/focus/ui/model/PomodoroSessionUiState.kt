package com.fakhry.pomodojo.features.focus.ui.model

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
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
    val isShowConfirmSkipDialog: Boolean = false,
    val isComplete: Boolean = false,
    val awaitingContinue: Boolean = false,
    val finishedPhaseType: TimerTypeUi? = null,
    val nextPhaseType: TimerTypeUi? = null,
    val nextPhaseDurationMs: Long = 0L,
)

sealed class PomodoroSessionSideEffect {
    data class ShowEndSessionDialog(val isShown: Boolean) : PomodoroSessionSideEffect()
    data class ShowSkipBreakDialog(val isShown: Boolean) : PomodoroSessionSideEffect()
    data class OnSessionComplete(
        val completionResult: PomodoroCompletionUiState,
    ) : PomodoroSessionSideEffect()
}
