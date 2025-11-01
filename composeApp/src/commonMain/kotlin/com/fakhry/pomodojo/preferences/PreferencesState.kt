package com.fakhry.pomodojo.preferences

data class PreferenceOption<T>(
    val label: String,
    val value: T,
    val selected: Boolean,
    val enabled: Boolean = true,
)

data class PreferencesState(
    val repeatCount: Int = PomodoroPreferences.DEFAULT_REPEAT_COUNT,
    val repeatRange: IntRange = DEFAULT_REPEAT_RANGE,
    val focusOptions: List<PreferenceOption<Int>> = emptyList(),
    val breakOptions: List<PreferenceOption<Int>> = emptyList(),
    val isLongBreakEnabled: Boolean = true,
    val longBreakAfterOptions: List<PreferenceOption<Int>> = emptyList(),
    val longBreakOptions: List<PreferenceOption<Int>> = emptyList(),
    val timelineSegments: List<TimelineSegment> = emptyList(),
    val isLoading: Boolean = true,
) {
    companion object {
        val DEFAULT_REPEAT_RANGE = 2..8
    }
}
