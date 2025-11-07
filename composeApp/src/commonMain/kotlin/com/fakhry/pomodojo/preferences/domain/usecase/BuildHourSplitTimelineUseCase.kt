package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain

class BuildHourSplitTimelineUseCase {

    operator fun invoke(preferences: PreferencesDomain): List<Int> {
        val completedCycles = preferences.repeatCount - 1
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
