package com.fakhry.pomodojo.features.focus.ui.viewmodel

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState

data class PreparedSession(
    val uiState: PomodoroSessionUiState,
    val snapshot: PomodoroSessionDomain,
    val didMutateTimeline: Boolean,
)
