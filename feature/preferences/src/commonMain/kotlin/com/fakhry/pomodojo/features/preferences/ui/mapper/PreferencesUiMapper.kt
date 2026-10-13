package com.fakhry.pomodojo.features.preferences.ui.mapper

import com.fakhry.pomodojo.core.designsystem.model.PreferenceOption
import com.fakhry.pomodojo.core.designsystem.model.TimelineUiModel
import com.fakhry.pomodojo.core.utils.flags.isDebugBuild
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.planTimeline
import com.fakhry.pomodojo.domain.preferences.model.AppTheme
import com.fakhry.pomodojo.domain.preferences.model.InitAppPreferences
import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences
import com.fakhry.pomodojo.features.preferences.ui.model.PreferencesUiModel
import kotlinx.collections.immutable.toPersistentList

// Debug builds also offer 1-minute Segments, so the Maestro flows finish in minutes.
private val DEBUG_MINUTES_OPTIONS = if (isDebugBuild) listOf(1) else emptyList()
private val FOCUS_OPTIONS = DEBUG_MINUTES_OPTIONS + PomodoroPreferences.FOCUS_MINUTES_OPTIONS
private val BREAK_OPTIONS = DEBUG_MINUTES_OPTIONS + PomodoroPreferences.BREAK_MINUTES_OPTIONS

fun PomodoroPreferences.mapToUiModel(initPreferences: InitAppPreferences): PreferencesUiModel {
    val longBreakEnabled = longBreakEnabled
    val themeOptions = AppTheme.entries.map { theme ->
        PreferenceOption(
            label = theme.displayName,
            value = theme,
            selected = initPreferences.appTheme == theme.storageValue,
        )
    }.toPersistentList()

    return PreferencesUiModel(
        selectedTheme = AppTheme.fromStorage(initPreferences.appTheme),
        themeOptions = themeOptions,
        isAlwaysOnDisplayEnabled = alwaysOnDisplayEnabled,
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
        longBreakAfterOptions = PomodoroPreferences.LONG_BREAK_AFTER_OPTIONS.map { count ->
            PreferenceOption(
                label = "$count focuses",
                value = count,
                selected = longBreakAfter == count,
                enabled = longBreakEnabled,
            )
        }.toPersistentList(),
        longBreakOptions = PomodoroPreferences.LONG_BREAK_MINUTES_OPTIONS.map { minutes ->
            PreferenceOption(
                label = "$minutes mins",
                value = minutes,
                selected = longBreakMinutes == minutes,
                enabled = longBreakEnabled,
            )
        }.toPersistentList(),
        timeline = previewTimeline(),
        isLoading = false,
    )
}

/** The Timeline plan for these preferences, drawn as if every Segment were done. */
private fun PomodoroPreferences.previewTimeline(): TimelineUiModel {
    val plan = planTimeline(this)
    return TimelineUiModel(
        segments = plan.segments
            .map { it.copy(timerStatus = TimerStatusDomain.COMPLETED) }
            .mapToTimelineSegmentsUi(),
        hourSplits = plan.hourSplits.toPersistentList(),
    )
}
