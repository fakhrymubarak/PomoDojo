package com.fakhry.pomodojo.preferences

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModelTest {

    @Test
    fun `initial state mirrors stored preferences`() = runTest {
        val storage = FakePreferenceStorage()
        val repository = PreferencesRepository(
            storage = storage,
            cascadeResolver = PreferenceCascadeResolver(),
            validator = PreferencesValidator,
        )
        val scope = TestScope(StandardTestDispatcher(testScheduler))
        val viewModel = PreferencesViewModel(
            repository = repository,
            timelineBuilder = TimelinePreviewBuilder(),
            scope = scope,
        )

        advanceUntilIdle()

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(PomodoroPreferences.DEFAULT_REPEAT_COUNT, state.repeatCount)
        assertTrue(state.focusOptions.first { it.value == 25 }.selected)
        assertEquals(8, state.timelineSegments.size) // 4 focus + 3 short breaks + 1 long break
        scope.cancel()
    }

    @Test
    fun `selecting focus option cascades updates`() = runTest {
        val storage = FakePreferenceStorage()
        val repository = PreferencesRepository(
            storage = storage,
            cascadeResolver = PreferenceCascadeResolver(),
            validator = PreferencesValidator,
        )
        val scope = TestScope(StandardTestDispatcher(testScheduler))
        val viewModel = PreferencesViewModel(
            repository = repository,
            timelineBuilder = TimelinePreviewBuilder(),
            scope = scope,
        )

        advanceUntilIdle()

        viewModel.onFocusOptionSelected(50)
        advanceUntilIdle()

        val state = viewModel.state.value

        assertTrue(state.focusOptions.first { it.value == 50 }.selected)
        assertTrue(state.breakOptions.first { it.value == 10 }.selected)
        assertTrue(state.longBreakAfterOptions.first { it.value == 2 }.selected)
        assertTrue(state.longBreakOptions.first { it.value == 20 }.selected)
        assertEquals(2, state.timelineSegments.count { it is TimelineSegment.LongBreak })
        scope.cancel()
    }

    private class FakePreferenceStorage : PreferenceStorage {
        private val state = MutableStateFlow(PomodoroPreferences())

        override val data = state

        override suspend fun update(transform: (PomodoroPreferences) -> PomodoroPreferences) {
            state.update(transform)
        }
    }
}
