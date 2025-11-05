package com.fakhry.pomodojo.preferences

import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildFocusTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesValidator
import com.fakhry.pomodojo.preferences.ui.PreferencesViewModel
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUiModel
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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
        val testDispatcher = Dispatchers.Unconfined
        val viewModel = PreferencesViewModel(
            repository = repository,
            timelineBuilder = BuildFocusTimelineUseCase(),
            dispatcher = DispatcherProvider(testDispatcher),
        )

        advanceUntilIdle()

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(PreferencesDomain.DEFAULT_REPEAT_COUNT, state.repeatCount)
        assertTrue(state.focusOptions.first { it.value == 25 }.selected)
        assertEquals(8, state.timelineSegments.size) // 4 focus + 3 short breaks + long break
    }

    @Test
    fun `selecting focus option cascades updates`() = runTest {
        val storage = FakePreferenceStorage()
        val repository = PreferencesRepository(
            storage = storage,
            cascadeResolver = PreferenceCascadeResolver(),
            validator = PreferencesValidator,
        )
        val testDispatcher = Dispatchers.Unconfined
        val viewModel = PreferencesViewModel(
            repository = repository,
            timelineBuilder = BuildFocusTimelineUseCase(),
            dispatcher = DispatcherProvider(testDispatcher),
        )

        advanceUntilIdle()

        viewModel.onFocusOptionSelected(50)
        advanceUntilIdle()

        val state = viewModel.state.value

        assertTrue(state.focusOptions.first { it.value == 50 }.selected)
        assertTrue(state.breakOptions.first { it.value == 10 }.selected)
        assertTrue(state.longBreakAfterOptions.first { it.value == 2 }.selected)
        assertTrue(state.longBreakOptions.first { it.value == 20 }.selected)
        assertEquals(2, state.timelineSegments.count { it is TimelineSegmentUiModel.LongBreak })
    }

    private class FakePreferenceStorage : PreferenceStorage {
        private val state = MutableStateFlow(PreferencesDomain())

        override val preferences = state

        override suspend fun update(transform: (PreferencesDomain) -> PreferencesDomain) {
            state.update(transform)
        }
    }
}
