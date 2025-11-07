package com.fakhry.pomodojo.preferences.ui.model

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain.Companion.DEFAULT_REPEAT_COUNT
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
    val timelineSegments: ImmutableList<TimelineSegmentUiModel> = persistentListOf(),
    val timelineHourSplits: ImmutableList<Int> = persistentListOf(),
    val isLoading: Boolean = true,
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
 */
@Immutable
sealed class TimelineSegmentUiModel(open val duration: Int) {
    @Immutable
    data class Focus(override val duration: Int) : TimelineSegmentUiModel(duration)

    @Immutable
    data class ShortBreak(override val duration: Int) : TimelineSegmentUiModel(duration)

    @Immutable
    data class LongBreak(override val duration: Int) : TimelineSegmentUiModel(duration)
}