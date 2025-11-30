package com.fakhry.pomodojo.domain.pomodoro.usecase

import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences

class BuildHourSplitTimelineUseCase {
    operator fun invoke(preferences: PomodoroPreferences): List<Int> {
        val completedCycles = preferences.repeatCount
        val hasLongBreaks = preferences.longBreakEnabled && preferences.longBreakAfter > 0
        val longBreaks = if (hasLongBreaks) completedCycles / preferences.longBreakAfter else 0
        val shortBreaks = completedCycles - longBreaks

        val totalDuration = preferences.repeatCount * preferences.focusMinutes +
            shortBreaks * preferences.breakMinutes +
            longBreaks * preferences.longBreakMinutes

        val fullHours = totalDuration / 60
        val remainder = totalDuration % 60

        val hourSplits = mutableListOf<Int>()
        repeat(fullHours) {
            hourSplits.add(60)
        }
        if (remainder != 0) hourSplits.add(remainder)

        return hourSplits
    }
}
