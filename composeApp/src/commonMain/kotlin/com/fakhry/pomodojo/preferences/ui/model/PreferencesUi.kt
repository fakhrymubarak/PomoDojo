package com.fakhry.pomodojo.preferences.ui.model

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain.Companion.DEFAULT_REPEAT_COUNT
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.preferences.ui.mapper.DEFAULT_REPEAT_RANGE
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class PreferencesUiModel(
    val selectedTheme: AppTheme = AppTheme.DARK,
    val themeOptions: ImmutableList<PreferenceOption<AppTheme>> = persistentListOf(),
    val repeatCount: Int = DEFAULT_REPEAT_COUNT,
    val repeatRange: IntRange = DEFAULT_REPEAT_RANGE,
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
)

@Immutable
data class TimelineUiModel(
    val segments: ImmutableList<TimelineSegmentUi> = persistentListOf(),
    val hourSplits: ImmutableList<Int> = persistentListOf(),
)

@Immutable
data class PreferenceOption<T>(
    val label: String,
    val value: T,
    val selected: Boolean,
    val enabled: Boolean = true,
)

/**
 * Represents a single segment in the Pomodoro timeline.
 * Each segment has a specific type and duration.
 *
 * @property duration The duration of the timeline segment in MINUTES.
 * @property progress The progress of the timeline segment from 0f to 1f.
 */
@Immutable
data class TimelineSegmentUi(
    val type: TimerType = TimerType.FOCUS,
    val cycleNumber: Int = 0,
    val timer: TimerUi = TimerUi(),
    val timerStatus: TimerStatusDomain = TimerStatusDomain.Initial,
)

@Immutable
data class TimerUi(
    val progress: Float = 0f,
    val durationEpochMs: Long = 0L,
    val formattedTime: String = "00:00",
    val finishedInMillis: Long = 0L,
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
)
