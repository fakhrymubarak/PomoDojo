package com.fakhry.pomodojo.domain.preferences.model

data class PomodoroPreferences(
    val repeatCount: Int = DEFAULT_REPEAT_COUNT,
    val focusMinutes: Int = DEFAULT_FOCUS_MINUTES,
    val breakMinutes: Int = DEFAULT_BREAK_MINUTES,
    val longBreakEnabled: Boolean = true,
    val longBreakAfter: Int = DEFAULT_LONG_BREAK_AFTER,
    val longBreakMinutes: Int = DEFAULT_LONG_BREAK_MINUTES,
    val alwaysOnDisplayEnabled: Boolean = false,
) {
    companion object {
        const val DEFAULT_REPEAT_COUNT = 4
        const val DEFAULT_FOCUS_MINUTES = 25
        const val DEFAULT_BREAK_MINUTES = 5
        const val DEFAULT_LONG_BREAK_AFTER = 4
        const val DEFAULT_LONG_BREAK_MINUTES = 10

        // The values the settings screen offers; the cascade moves between them.
        val REPEAT_COUNT_OPTIONS = 2..8
        val FOCUS_MINUTES_OPTIONS = listOf(10, 25, 50)
        val BREAK_MINUTES_OPTIONS = listOf(2, 5, 10)
        val LONG_BREAK_AFTER_OPTIONS = listOf(6, 4, 2)
        val LONG_BREAK_MINUTES_OPTIONS = listOf(4, 10, 20)
    }
}
