package com.fakhry.pomodojo.features.preferences.ui.model

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.core.designsystem.model.PreferenceOption
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import com.fakhry.pomodojo.domain.preferences.model.AppTheme
import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences.Companion.DEFAULT_REPEAT_COUNT
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class PreferencesUiModel(
    val selectedTheme: AppTheme = AppTheme.DARK,
    val themeOptions: ImmutableList<PreferenceOption<AppTheme>> = persistentListOf(),
    val isAlwaysOnDisplayEnabled: Boolean = false,
    val repeatCount: Int = DEFAULT_REPEAT_COUNT,
    val repeatRange: IntRange = 2..8,
    val focusOptions: ImmutableList<PreferenceOption<Int>> = persistentListOf(),
    val breakOptions: ImmutableList<PreferenceOption<Int>> = persistentListOf(),
    val isLongBreakEnabled: Boolean = true,
    val longBreakAfterOptions: ImmutableList<PreferenceOption<Int>> = persistentListOf(),
    val longBreakOptions: ImmutableList<PreferenceOption<Int>> = persistentListOf(),
    val timeline: TimelineUiModel = TimelineUiModel(),
    val isLoading: Boolean = true,
)

@Immutable
data class PreferencesConfigUiState(
    val repeatCount: Int,
    val repeatRange: IntRange,
    val focusOptions: ImmutableList<PreferenceOption<Int>>,
    val breakOptions: ImmutableList<PreferenceOption<Int>>,
    val isLongBreakEnabled: Boolean,
    val longBreakAfterOptions: ImmutableList<PreferenceOption<Int>>,
    val longBreakOptions: ImmutableList<PreferenceOption<Int>>,
)

@Immutable
data class PreferencesAppearanceUiState(
    val themeOptions: ImmutableList<PreferenceOption<AppTheme>>,
    val isAlwaysOnDisplayEnabled: Boolean,
)

fun PreferencesUiModel.toConfigUiState() = PreferencesConfigUiState(
    repeatCount = repeatCount,
    repeatRange = repeatRange,
    focusOptions = focusOptions,
    breakOptions = breakOptions,
    isLongBreakEnabled = isLongBreakEnabled,
    longBreakAfterOptions = longBreakAfterOptions,
    longBreakOptions = longBreakOptions,
)

fun PreferencesUiModel.toAppearanceUiState() = PreferencesAppearanceUiState(
    themeOptions = themeOptions,
    isAlwaysOnDisplayEnabled = isAlwaysOnDisplayEnabled,
)
