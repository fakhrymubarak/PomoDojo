package com.fakhry.pomodojo.domain.preferences.usecase

import com.fakhry.pomodojo.domain.preferences.model.BreakCascade
import com.fakhry.pomodojo.domain.preferences.model.FocusCascade

class PreferenceCascadeResolver {
    @Throws(IllegalArgumentException::class)
    fun resolveForFocus(minutes: Int): FocusCascade = when (minutes) {
        1 -> FocusCascade(
            breakMinutes = 1,
            longBreakAfterCount = 2,
            longBreakMinutes = 2,
        )
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
