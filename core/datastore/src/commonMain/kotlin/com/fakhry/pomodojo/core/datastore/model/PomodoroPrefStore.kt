package com.fakhry.pomodojo.core.datastore.model

data class PomodoroPrefStore(
    val repeatCount: Int = DEFAULT_REPEAT_COUNT,
    val focusMinutes: Int = DEFAULT_FOCUS_MINUTES,
    val breakMinutes: Int = DEFAULT_BREAK_MINUTES,
    val longBreakEnabled: Boolean = true,
    val longBreakAfter: Int = DEFAULT_LONG_BREAK_AFTER,
    val longBreakMinutes: Int = DEFAULT_LONG_BREAK_MINUTES,
    val alwaysOnDisplayEnabled: Boolean = false,
) {
    companion object Companion {
        const val DEFAULT_REPEAT_COUNT = 4
        const val DEFAULT_FOCUS_MINUTES = 25
        const val DEFAULT_BREAK_MINUTES = 5
        const val DEFAULT_LONG_BREAK_AFTER = 4
        const val DEFAULT_LONG_BREAK_MINUTES = 10
    }
}
