package com.fakhry.pomodojo.preferences.ui.mapper

import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineSegmentDomain
import com.fakhry.pomodojo.preferences.ui.model.PreferenceOption
import com.fakhry.pomodojo.preferences.ui.model.PreferencesUiModel
import kotlinx.collections.immutable.toPersistentList

private val FOCUS_OPTIONS = listOf(10, 25, 50)
private val BREAK_OPTIONS = listOf(2, 5, 10)
private val LONG_BREAK_AFTER = listOf(6, 4, 2)
private val LONG_BREAK_MINUTES = listOf(4, 10, 20)
val DEFAULT_REPEAT_RANGE = 2..8

fun PreferencesDomain.mapToUiModel(
    timelineBuilder: (PreferencesDomain) -> List<TimelineSegmentDomain>,
    hourSplitter: (PreferencesDomain) -> List<Int>,
) = this.run {
    val longBreakEnabled = longBreakEnabled
    val themeOptions = AppTheme.entries.map { theme ->
        PreferenceOption(
            label = theme.displayName,
            value = theme,
            selected = appTheme == theme,
        )
    }.toPersistentList()

    return@run PreferencesUiModel(
        selectedTheme = appTheme,
        themeOptions = themeOptions,
        repeatCount = repeatCount,
        focusOptions = FOCUS_OPTIONS.map { minutes ->
            PreferenceOption(
                label = "$minutes mins",
                value = minutes,
                selected = focusMinutes == minutes,
            )
        }.toPersistentList(),
        breakOptions = BREAK_OPTIONS.map { minutes ->
            PreferenceOption(
                label = "$minutes mins",
                value = minutes,
                selected = breakMinutes == minutes,
            )
        }.toPersistentList(),
        isLongBreakEnabled = longBreakEnabled,
        longBreakAfterOptions = LONG_BREAK_AFTER.map { count ->
            PreferenceOption(
                label = "$count focuses",
                value = count,
                selected = longBreakAfter == count,
                enabled = longBreakEnabled,
            )
        }.toPersistentList(),
        longBreakOptions = LONG_BREAK_MINUTES.map { minutes ->
            PreferenceOption(
                label = "$minutes mins",
                value = minutes,
                selected = longBreakMinutes == minutes,
                enabled = longBreakEnabled,
            )
        }.toPersistentList(),
        timelineSegments = timelineBuilder(this).mapToTimelineSegmentsUi(),
        timelineHourSplits = hourSplitter(this).toPersistentList(),
        isLoading = false,
    )
}