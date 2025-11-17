package com.fakhry.pomodojo.features.focus.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class PomodoroCompletionUiState(
    val totalCyclesFinished: Int = 0,
    val totalFocusMinutes: Int = 0,
    val totalBreakMinutes: Int = 0,
) {
    fun isEmpty() = this == PomodoroCompletionUiState()
}
