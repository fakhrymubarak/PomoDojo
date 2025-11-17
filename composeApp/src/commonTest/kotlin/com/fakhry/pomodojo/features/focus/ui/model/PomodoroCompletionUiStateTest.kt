package com.fakhry.pomodojo.features.focus.ui.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PomodoroCompletionUiStateTest {
    @Test
    fun `default state is empty`() {
        assertTrue(PomodoroCompletionUiState().isEmpty())
    }

    @Test
    fun `populated state is not empty`() {
        val state =
            PomodoroCompletionUiState(
                totalCyclesFinished = 2,
                totalFocusMinutes = 40,
                totalBreakMinutes = 10,
            )

        assertFalse(state.isEmpty())
    }
}
