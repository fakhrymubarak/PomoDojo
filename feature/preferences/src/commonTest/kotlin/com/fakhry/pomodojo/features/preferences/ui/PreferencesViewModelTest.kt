package com.fakhry.pomodojo.features.preferences.ui

import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.domain.preferences.model.AppTheme
import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences
import com.fakhry.pomodojo.domain.preferences.repository.PreferencesRepository
import com.fakhry.pomodojo.domain.preferences.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.features.preferences.domain.model.InitAppPreferences
import com.fakhry.pomodojo.features.preferences.domain.repository.InitPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val fixture = createFixture()
        val viewModel = fixture.viewModel

        advanceUntilIdle()

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(PomodoroPreferences.DEFAULT_REPEAT_COUNT, state.repeatCount)
        assertTrue(state.focusOptions.first { it.value == 25 }.selected)
        assertEquals(8, state.timeline.segments.size) // 4 focus + 2 long breaks + 2 short breaks
    }

    @Test
    fun `selecting focus option cascades updates`() = runTest {
        val fixture = createFixture()
        val viewModel = fixture.viewModel

        advanceUntilIdle()

        viewModel.onFocusOptionSelected(50)
        advanceUntilIdle()

        val state = viewModel.state.value

        assertTrue(state.focusOptions.first { it.value == 50 }.selected)
        assertTrue(state.breakOptions.first { it.value == 10 }.selected)
        assertTrue(state.longBreakAfterOptions.first { it.value == 2 }.selected)
        assertTrue(state.longBreakOptions.first { it.value == 20 }.selected)
        assertEquals(2, state.timeline.segments.count { it.type == TimerTypeUi.LONG_BREAK })
    }

    @Test
    fun `unrelated preference keeps immutable option references`() = runTest {
        val fixture = createFixture()
        val viewModel = fixture.viewModel

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
        val fixture = createFixture()
        val viewModel = fixture.viewModel

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
        val fixture = createFixture()
        val viewModel = fixture.viewModel

        advanceUntilIdle()
        val initialSegmentCount = viewModel.state.value.timeline.segments.size

        viewModel.onRepeatCountChanged(6)
        advanceUntilIdle()

        assertEquals(6, viewModel.state.value.repeatCount)
        assertTrue(viewModel.state.value.timeline.segments.size > initialSegmentCount)
    }

    @Test
    fun `onBreakOptionSelected updates break minutes`() = runTest {
        val fixture = createFixture()
        val viewModel = fixture.viewModel

        advanceUntilIdle()

        viewModel.onBreakOptionSelected(10)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.breakOptions.first { it.value == 10 }.selected)
    }

    @Test
    fun `onLongBreakEnabledToggled enables long break`() = runTest {
        val fixture = createFixture(
            preferences = PomodoroPreferences(longBreakEnabled = false),
        )
        val viewModel = fixture.viewModel

        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLongBreakEnabled)

        viewModel.onLongBreakEnabledToggled(true)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isLongBreakEnabled)
    }

    @Test
    fun `onLongBreakEnabledToggled with same value does not trigger update`() = runTest {
        val fixture = createFixture()
        val viewModel = fixture.viewModel

        advanceUntilIdle()
        val initialState = viewModel.state.value

        viewModel.onLongBreakEnabledToggled(true)
        advanceUntilIdle()

        assertEquals(initialState.isLongBreakEnabled, viewModel.state.value.isLongBreakEnabled)
    }

    @Test
    fun `onLongBreakAfterSelected updates long break interval`() = runTest {
        val fixture = createFixture()
        val viewModel = fixture.viewModel

        advanceUntilIdle()

        viewModel.onLongBreakAfterSelected(6)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.longBreakAfterOptions.first { it.value == 6 }.selected)
    }

    @Test
    fun `onLongBreakMinutesSelected updates long break duration`() = runTest {
        val fixture = createFixture()
        val viewModel = fixture.viewModel

        advanceUntilIdle()

        viewModel.onLongBreakMinutesSelected(20)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.longBreakOptions.first { it.value == 20 }.selected)
    }

    @Test
    fun `onThemeSelected updates theme in appearance state`() = runTest {
        val fixture = createFixture()
        val viewModel = fixture.viewModel

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
        val fixture = createFixture()
        val viewModel = fixture.viewModel

        advanceUntilIdle()
        val initialState = viewModel.appearanceState.value

        viewModel.onAlwaysOnDisplayToggled(false)
        advanceUntilIdle()

        assertEquals(
            initialState.isAlwaysOnDisplayEnabled,
            viewModel.appearanceState.value.isAlwaysOnDisplayEnabled,
        )
    }

    @Test
    fun `isLoadingState becomes false after initialization`() = runTest {
        val fixture = createFixture()
        val viewModel = fixture.viewModel

        advanceUntilIdle()

        val loadingState = viewModel.isLoadingState.filter { !it }.first()
        assertFalse(loadingState)
    }
}

private data class PreferencesFixture(
    val viewModel: PreferencesViewModel,
    val preferencesRepo: InMemoryPreferencesRepository,
    val initRepo: InMemoryInitPreferencesRepository,
)

private fun createFixture(
    preferences: PomodoroPreferences = PomodoroPreferences(),
    initPrefs: InitAppPreferences = InitAppPreferences(),
): PreferencesFixture {
    val prefRepo = InMemoryPreferencesRepository(preferences)
    val initRepo = InMemoryInitPreferencesRepository(initPrefs)
    val dispatcher = DispatcherProvider(Dispatchers.Unconfined)
    val viewModel =
        PreferencesViewModel(
            repository = prefRepo,
            initPreferencesRepository = initRepo,
            timelineBuilder = BuildTimerSegmentsUseCase(),
            hourSplitter = BuildHourSplitTimelineUseCase(),
            dispatcher = dispatcher,
        )
    return PreferencesFixture(viewModel, prefRepo, initRepo)
}

private class InMemoryPreferencesRepository(
    initial: PomodoroPreferences = PomodoroPreferences(),
    private val cascadeResolver: PreferenceCascadeResolver = PreferenceCascadeResolver(),
) : PreferencesRepository {
    private val state = MutableStateFlow(initial)

    override val preferences: Flow<PomodoroPreferences> = state.asStateFlow()

    override suspend fun updateRepeatCount(value: Int) {
        state.update { it.copy(repeatCount = value) }
    }

    override suspend fun updateFocusMinutes(value: Int) {
        val cascade = cascadeResolver.resolveForFocus(value)
        state.update {
            it.copy(
                focusMinutes = value,
                breakMinutes = cascade.breakMinutes,
                longBreakAfter = cascade.longBreakAfterCount,
                longBreakMinutes = cascade.longBreakMinutes,
            )
        }
    }

    override suspend fun updateBreakMinutes(value: Int) {
        val cascade = cascadeResolver.resolveForBreak(value)
        state.update {
            it.copy(
                breakMinutes = value,
                longBreakMinutes = cascade.longBreakMinutes,
            )
        }
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        state.update { it.copy(longBreakEnabled = enabled) }
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        state.update { it.copy(longBreakAfter = value) }
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        state.update { it.copy(longBreakMinutes = value) }
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        state.update { it.copy(alwaysOnDisplayEnabled = enabled) }
    }
}

private class InMemoryInitPreferencesRepository(
    initial: InitAppPreferences = InitAppPreferences(),
) : InitPreferencesRepository {
    private val state = MutableStateFlow(initial)
    override val initPreferences: Flow<InitAppPreferences> = state.asStateFlow()

    override suspend fun updateAppTheme(theme: AppTheme) {
        state.update { it.copy(appTheme = theme.storageValue) }
    }

    override suspend fun updateHasActiveSession(value: Boolean) {
        state.update { it.copy(hasActiveSession = value) }
    }
}
