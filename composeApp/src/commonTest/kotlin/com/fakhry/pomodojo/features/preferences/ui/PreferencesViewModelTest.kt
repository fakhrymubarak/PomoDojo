package com.fakhry.pomodojo.features.preferences.ui

import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.features.preferences.data.repository.PreferencesRepositoryImpl
import com.fakhry.pomodojo.features.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.features.preferences.domain.model.TimerType
import com.fakhry.pomodojo.features.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.features.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.features.preferences.domain.usecase.PreferenceCascadeResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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
        assertEquals(8, state.timeline.segments.size) // 4 focus + 2 long breaks + 2 short breaks
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
        assertEquals(2, state.timeline.segments.count { it.type == TimerType.LONG_BREAK })
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

    @Test
    fun `always on display toggle updates appearance state`() = runTest {
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
        assertFalse(viewModel.appearanceState.value.isAlwaysOnDisplayEnabled)

        viewModel.onAlwaysOnDisplayToggled(true)
        advanceUntilIdle()

        val updatedAppearance =
            viewModel.appearanceState.filter { it.isAlwaysOnDisplayEnabled }.first()
        assertTrue(updatedAppearance.isAlwaysOnDisplayEnabled)
    }

    @Test
    fun `onRepeatCountChanged updates repeat count and timeline`() = runTest {
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
        val initialSegmentCount = viewModel.state.value.timeline.segments.size

        viewModel.onRepeatCountChanged(6)
        advanceUntilIdle()

        assertEquals(6, viewModel.state.value.repeatCount)
        assertTrue(viewModel.state.value.timeline.segments.size > initialSegmentCount)
    }

    @Test
    fun `onBreakOptionSelected updates break minutes`() = runTest {
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

        viewModel.onBreakOptionSelected(10)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.breakOptions.first { it.value == 10 }.selected)
    }

    @Test
    fun `onLongBreakEnabledToggled enables long break`() = runTest {
        val storage = FakePreferenceStorage()
        storage.state.value = PreferencesDomain(longBreakEnabled = false)
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
        assertFalse(viewModel.state.value.isLongBreakEnabled)

        viewModel.onLongBreakEnabledToggled(true)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isLongBreakEnabled)
    }

    @Test
    fun `onLongBreakEnabledToggled with same value does not trigger update`() = runTest {
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

        // Toggle with the same value (already true)
        viewModel.onLongBreakEnabledToggled(true)
        advanceUntilIdle()

        // State should remain the same
        assertEquals(initialState.isLongBreakEnabled, viewModel.state.value.isLongBreakEnabled)
    }

    @Test
    fun `onLongBreakAfterSelected updates long break interval`() = runTest {
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

        viewModel.onLongBreakAfterSelected(6)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.longBreakAfterOptions.first { it.value == 6 }.selected)
    }

    @Test
    fun `onLongBreakMinutesSelected updates long break duration`() = runTest {
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

        viewModel.onLongBreakMinutesSelected(20)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.longBreakOptions.first { it.value == 20 }.selected)
    }

    @Test
    fun `onThemeSelected updates theme in appearance state`() = runTest {
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

        viewModel.onThemeSelected(AppTheme.DARK)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(
            state.themeOptions.first {
                it.value == AppTheme.DARK
            }.selected,
        )
    }

    @Test
    fun `onAlwaysOnDisplayToggled with same value does not trigger update`() = runTest {
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
        val initialState = viewModel.appearanceState.value

        // Toggle with the same value (already false)
        viewModel.onAlwaysOnDisplayToggled(false)
        advanceUntilIdle()

        // State should remain the same
        assertEquals(
            initialState.isAlwaysOnDisplayEnabled,
            viewModel.appearanceState.value.isAlwaysOnDisplayEnabled,
        )
    }

    @Test
    fun `isLoadingState becomes false after initialization`() = runTest {
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

        // Wait for the loading state to actually emit false
        val loadingState = viewModel.isLoadingState.filter { !it }.first()
        assertFalse(loadingState)
    }

    private class FakePreferenceStorage : PreferenceStorage {
        val state = MutableStateFlow(PreferencesDomain())

        override val preferences = state

        override suspend fun update(transform: (PreferencesDomain) -> PreferencesDomain) {
            state.update(transform)
        }
    }
}
