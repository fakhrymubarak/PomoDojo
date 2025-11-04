package com.fakhry.pomodojo.preferences.ui.state

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.preferences.domain.AppTheme
import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegment
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class PreferenceOption<T>(
    val label: String,
    val value: T,
    val selected: Boolean,
    val enabled: Boolean = true,
)

@Immutable
data class PreferencesState(
    val selectedTheme: AppTheme = AppTheme.DARK,
    val themeOptions: ImmutableList<PreferenceOption<AppTheme>> = persistentListOf(),
    val repeatCount: Int = PomodoroPreferences.Companion.DEFAULT_REPEAT_COUNT,
    val repeatRange: IntRange = DEFAULT_REPEAT_RANGE,
    val focusOptions: ImmutableList<PreferenceOption<Int>> = persistentListOf(),
    val breakOptions: ImmutableList<PreferenceOption<Int>> = persistentListOf(),
    val isLongBreakEnabled: Boolean = true,
    val longBreakAfterOptions: ImmutableList<PreferenceOption<Int>> = persistentListOf(),
    val longBreakOptions: ImmutableList<PreferenceOption<Int>> = persistentListOf(),
    val timelineSegments: ImmutableList<TimelineSegment> = persistentListOf(),
    val isLoading: Boolean = true,
) {
    companion object {
        val DEFAULT_REPEAT_RANGE = 2..8
    }
}
