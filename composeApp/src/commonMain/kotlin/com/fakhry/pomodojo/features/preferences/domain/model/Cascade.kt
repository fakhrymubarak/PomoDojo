package com.fakhry.pomodojo.features.preferences.domain.model

data class FocusCascade(
    val breakMinutes: Int,
    val longBreakAfterCount: Int,
    val longBreakMinutes: Int,
)

data class BreakCascade(val longBreakAfterCount: Int, val longBreakMinutes: Int)
