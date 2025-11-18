package com.fakhry.pomodojo.features.preferences.ui.mapper

import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.InitAppPreferences
import com.fakhry.pomodojo.features.preferences.domain.model.PomodoroPreferences
import com.fakhry.pomodojo.features.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.features.preferences.ui.model.PreferenceOption
import com.fakhry.pomodojo.features.preferences.ui.model.PreferencesUiModel
import com.fakhry.pomodojo.features.preferences.ui.model.TimelineUiModel
import kotlinx.collections.immutable.toPersistentList

private val FOCUS_OPTIONS = listOf(10, 25, 50)
private val BREAK_OPTIONS = listOf(2, 5, 10)
private val LONG_BREAK_AFTER = listOf(6, 4, 2)
private val LONG_BREAK_MINUTES = listOf(4, 10, 20)
val DEFAULT_REPEAT_RANGE = 2..8

fun PomodoroPreferences.mapToUiModel(
    initPreferences: InitAppPreferences,
    timelineBuilder: (PomodoroPreferences) -> List<TimerSegmentsDomain>,
    hourSplitter: (PomodoroPreferences) -> List<Int>,
): PreferencesUiModel {
    val longBreakEnabled = longBreakEnabled
    val themeOptions =
        AppTheme.entries
            .map { theme ->
                PreferenceOption(
                    label = theme.displayName,
                    value = theme,
                    selected = initPreferences.appTheme == theme,
                )
            }.toPersistentList()

    return PreferencesUiModel(
        selectedTheme = initPreferences.appTheme,
        themeOptions = themeOptions,
        isAlwaysOnDisplayEnabled = alwaysOnDisplayEnabled,
        repeatCount = repeatCount,
        focusOptions =
        FOCUS_OPTIONS
            .map { minutes ->
                PreferenceOption(
                    label = "$minutes mins",
                    value = minutes,
                    selected = focusMinutes == minutes,
                )
            }.toPersistentList(),
        breakOptions =
        BREAK_OPTIONS
            .map { minutes ->
                PreferenceOption(
                    label = "$minutes mins",
                    value = minutes,
                    selected = breakMinutes == minutes,
                )
            }.toPersistentList(),
        isLongBreakEnabled = longBreakEnabled,
        longBreakAfterOptions =
        LONG_BREAK_AFTER
            .map { count ->
                PreferenceOption(
                    label = "$count focuses",
                    value = count,
                    selected = longBreakAfter == count,
                    enabled = longBreakEnabled,
                )
            }.toPersistentList(),
        longBreakOptions =
        LONG_BREAK_MINUTES
            .map { minutes ->
                PreferenceOption(
                    label = "$minutes mins",
                    value = minutes,
                    selected = longBreakMinutes == minutes,
                    enabled = longBreakEnabled,
                )
            }.toPersistentList(),
        timeline =
        TimelineUiModel(
            segments = timelineBuilder(this).mapToTimelineSegmentsUi(),
            hourSplits = hourSplitter(this).toPersistentList(),
        ),
        isLoading = false,
    )
}
