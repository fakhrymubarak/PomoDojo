package com.fakhry.pomodojo.preferences.domain.model

data class PreferencesDomain(
    val appTheme: AppTheme = AppTheme.DARK,
    val repeatCount: Int = DEFAULT_REPEAT_COUNT,
    val focusMinutes: Int = DEFAULT_FOCUS_MINUTES,
    val breakMinutes: Int = DEFAULT_BREAK_MINUTES,
    val longBreakEnabled: Boolean = true,
    val longBreakAfter: Int = DEFAULT_LONG_BREAK_AFTER,
    val longBreakMinutes: Int = DEFAULT_LONG_BREAK_MINUTES,
) {
    val isLongBreakPoint : Boolean
        get() = longBreakEnabled && repeatCount % longBreakAfter == 0
    val focusMillis: Long
        get() = focusMinutes * 60_000L
    val breakMillis: Long
        get() = breakMinutes * 60_000L
    val longBreakMillis: Long
        get() = longBreakMinutes * 60_000L

    fun calculateTotalDurationInMinutes(): Int {
        var totalDuration = 0
        for (cycle in 1..repeatCount) {
            totalDuration += focusMinutes
            val isLongBreakPoint = longBreakEnabled && cycle % longBreakAfter == 0
            val isLastFocus = cycle == repeatCount

            if (!isLastFocus && isLongBreakPoint) {
                totalDuration += longBreakMinutes
            } else if (!isLastFocus) {
                totalDuration += breakMinutes
            }
        }
        return totalDuration
    }

    companion object {
        const val DEFAULT_REPEAT_COUNT = 4
        const val DEFAULT_FOCUS_MINUTES = 25
        const val DEFAULT_BREAK_MINUTES = 5
        const val DEFAULT_LONG_BREAK_AFTER = 4
        const val DEFAULT_LONG_BREAK_MINUTES = 10
    }
}