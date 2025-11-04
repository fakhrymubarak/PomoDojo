package com.fakhry.pomodojo.focus.ui

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.focus.domain.model.FocusPhase
import com.fakhry.pomodojo.focus.domain.model.FocusTimerStatus
import com.fakhry.pomodojo.focus.domain.model.QuoteContent

sealed interface FocusPomodoroUiState {
    @Immutable
    data object Loading : FocusPomodoroUiState

    @Immutable
    data class Active(
        val timerStatus: FocusTimerStatus,
        val remainingSeconds: Int,
        val totalSeconds: Int,
        val formattedTime: String,
        val progress: Float,
        val completedSegments: Int,
        val totalSegments: Int,
        val quote: QuoteContent,
        val phase: FocusPhase,
        val showConfirmEndDialog: Boolean,
    ) : FocusPomodoroUiState

    @Immutable
    data object Completing : FocusPomodoroUiState

    @Immutable
    data object Completed : FocusPomodoroUiState

    @Immutable
    data class Error(val message: String) : FocusPomodoroUiState
}
