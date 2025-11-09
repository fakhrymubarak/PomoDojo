package com.fakhry.pomodojo.focus.ui

fun PomodoroSessionUiState.toHeaderUiState(): PomodoroHeaderUiState =
    PomodoroHeaderUiState(totalCycle = totalCycle, activeSegment = activeSegment)
