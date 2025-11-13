package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildHourSplitTimelineUseCaseTest {
    private val useCase = BuildHourSplitTimelineUseCase()

    @Test
    fun `splits schedule into hours with configured long breaks`() {
        val preferences =
            PreferencesDomain(
                repeatCount = 6,
                focusMinutes = 25,
                breakMinutes = 5,
                longBreakEnabled = true,
                longBreakAfter = 2,
                longBreakMinutes = 15,
            )

        val result = useCase(preferences)

        // 6*25 + 3*15 + 3*5 = 150 + 45 + 15 = 210 min = 3h 30min
        assertEquals(listOf(60, 60, 60, 30), result)
    }

    @Test
    fun `falls back to short breaks when long breaks disabled`() {
        val preferences =
            PreferencesDomain(
                repeatCount = 3,
                focusMinutes = 20,
                breakMinutes = 5,
                longBreakEnabled = false,
                longBreakAfter = 1,
                longBreakMinutes = 40,
            )

        val result = useCase(preferences)

        // 3*20 + 3*5 = 60 + 15 = 75 min = 1h 15min
        assertEquals(listOf(60, 15), result)
    }

    @Test
    fun `returns remainder only when total duration below an hour`() {
        val preferences =
            PreferencesDomain(
                repeatCount = 1,
                focusMinutes = 45,
                breakMinutes = 5,
                longBreakEnabled = true,
                longBreakAfter = 1,
                longBreakMinutes = 10,
            )

        val result = useCase(preferences)

        // 1*45 + 1*10 = 55 min (long break after cycle 1)
        assertEquals(listOf(55), result)
    }
}
