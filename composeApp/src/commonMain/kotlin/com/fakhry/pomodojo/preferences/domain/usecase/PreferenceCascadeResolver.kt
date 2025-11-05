package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.BreakCascade
import com.fakhry.pomodojo.preferences.domain.model.FocusCascade

class PreferenceCascadeResolver {

    fun resolveForFocus(minutes: Int): FocusCascade = when (minutes) {
        10 -> FocusCascade(
            breakMinutes = 2,
            longBreakAfterCount = 6,
            longBreakMinutes = 4,
        )
        25 -> FocusCascade(
            breakMinutes = 5,
            longBreakAfterCount = 4,
            longBreakMinutes = 10,
        )
        50 -> FocusCascade(
            breakMinutes = 10,
            longBreakAfterCount = 2,
            longBreakMinutes = 20,
        )
        else -> throw IllegalArgumentException("Unsupported focus value: $minutes")
    }

    fun resolveForBreak(minutes: Int): BreakCascade = when (minutes) {
        2 -> BreakCascade(longBreakAfterCount = 6, longBreakMinutes = 4)
        5 -> BreakCascade(longBreakAfterCount = 4, longBreakMinutes = 10)
        10 -> BreakCascade(longBreakAfterCount = 2, longBreakMinutes = 20)
        else -> throw IllegalArgumentException("Unsupported break value: $minutes")
    }
}