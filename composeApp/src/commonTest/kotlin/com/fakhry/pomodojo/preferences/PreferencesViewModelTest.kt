package com.fakhry.pomodojo.preferences

import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepositoryImpl
import com.fakhry.pomodojo.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.ui.PreferencesViewModel
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
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModelTest {
    @Test
    fun `initial state mirrors stored preferences`() = runTest {
        val storage = FakePreferenceStorage()
        val repository =
            PreferencesRepositoryImpl(
                storage = storage,
                cascadeResolver = PreferenceCascadeResolver(),
            )
        val testDispatcher = Dispatchers.Unconfined
        val viewModel =
            PreferencesViewModel(
                repository = repository,
                timelineBuilder = BuildTimerSegmentsUseCase(),
                hourSplitter = BuildHourSplitTimelineUseCase(),
                dispatcher = DispatcherProvider(testDispatcher),
            )

        advanceUntilIdle()

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(PreferencesDomain.DEFAULT_REPEAT_COUNT, state.repeatCount)
        assertTrue(state.focusOptions.first { it.value == 25 }.selected)
        assertEquals(7, state.timeline.segments.size) // 4 focus + 3 short breaks + long break
    }

    @Test
    fun `selecting focus option cascades updates`() = runTest {
        val storage = FakePreferenceStorage()
        val repository =
            PreferencesRepositoryImpl(
                storage = storage,
                cascadeResolver = PreferenceCascadeResolver(),
            )
        val testDispatcher = Dispatchers.Unconfined
        val viewModel =
            PreferencesViewModel(
                repository = repository,
                timelineBuilder = BuildTimerSegmentsUseCase(),
                hourSplitter = BuildHourSplitTimelineUseCase(),
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
        assertEquals(1, state.timeline.segments.count { it.type == TimerType.LONG_BREAK })
    }

    @Test
    fun `unrelated preference keeps immutable option references`() = runTest {
        val storage = FakePreferenceStorage()
        val repository =
            PreferencesRepositoryImpl(
                storage = storage,
                cascadeResolver = PreferenceCascadeResolver(),
            )
        val testDispatcher = Dispatchers.Unconfined
        val viewModel =
            PreferencesViewModel(
                repository = repository,
                timelineBuilder = BuildTimerSegmentsUseCase(),
                hourSplitter = BuildHourSplitTimelineUseCase(),
                dispatcher = DispatcherProvider(testDispatcher),
            )

        advanceUntilIdle()
        val initialState = viewModel.state.value

        viewModel.onRepeatCountChanged(initialState.repeatCount + 1)
        advanceUntilIdle()

        val updatedState = viewModel.state.value

        assertSame(
            initialState.themeOptions,
            updatedState.themeOptions,
            "Theme options should keep the same reference.",
        )
        assertSame(
            initialState.focusOptions,
            updatedState.focusOptions,
            "Focus options should remain stable.",
        )
        assertSame(
            initialState.breakOptions,
            updatedState.breakOptions,
            "Break options should remain stable.",
        )
        assertSame(
            initialState.longBreakAfterOptions,
            updatedState.longBreakAfterOptions,
            "Long-break-after options should remain stable.",
        )
        assertSame(
            initialState.longBreakOptions,
            updatedState.longBreakOptions,
            "Long-break duration options should remain stable.",
        )
        assertNotSame(
            initialState.timeline.segments,
            updatedState.timeline.segments,
            "Timeline must change when repeat count updates.",
        )
    }

    private class FakePreferenceStorage : PreferenceStorage {
        private val state = MutableStateFlow(PreferencesDomain())

        override val preferences = state

        override suspend fun update(transform: (PreferencesDomain) -> PreferencesDomain) {
            state.update(transform)
        }
    }
}
