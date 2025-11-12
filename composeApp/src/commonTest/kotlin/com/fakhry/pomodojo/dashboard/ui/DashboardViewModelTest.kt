package com.fakhry.pomodojo.dashboard.ui

import com.fakhry.pomodojo.dashboard.ui.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.repository.ActiveSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.HistorySessionRepository
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.ui.state.DomainResult
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `pref state reflects initial preferences`() = runTest(dispatcher) {
        val repository = FakePreferencesRepository(PreferencesDomain(focusMinutes = 30))
        val focusRepository = FakeFocusRepository(hasActive = false)
        val historyRepository = FakeHistoryRepository()
        val dispatcherProvider = DispatcherProvider(dispatcher)
        val currentTimeProvider = FakeCurrentTimeProvider()

        val viewModel =
            DashboardViewModel(
                historyRepo = historyRepository,
                repository = repository,
                focusRepository = focusRepository,
                dispatcher = dispatcherProvider,
                currentTimeProvider = currentTimeProvider,
            )
        advanceUntilIdle()

        assertEquals(30, viewModel.prefState.value.focusMinutes)
        assertFalse(viewModel.hasActiveSession.value)
    }

    @Test
    fun `pref state updates when repository emits new value`() = runTest(dispatcher) {
        val repository = FakePreferencesRepository(PreferencesDomain(focusMinutes = 25))
        val focusRepository = FakeFocusRepository(hasActive = true)
        val historyRepository = FakeHistoryRepository()
        val dispatcherProvider = DispatcherProvider(dispatcher)
        val currentTimeProvider = FakeCurrentTimeProvider()

        val viewModel =
            DashboardViewModel(
                historyRepo = historyRepository,
                repository = repository,
                focusRepository = focusRepository,
                dispatcher = dispatcherProvider,
                currentTimeProvider = currentTimeProvider,
            )
        advanceUntilIdle()

        repository.emit(repository.current.copy(focusMinutes = 45))
        advanceUntilIdle()

        assertEquals(45, viewModel.prefState.value.focusMinutes)
        assertTrue(viewModel.hasActiveSession.value)
    }

    private class FakePreferencesRepository(initial: PreferencesDomain) : PreferencesRepository {
        private val state = MutableStateFlow(initial)
        val current: PreferencesDomain get() = state.value

        override val preferences: Flow<PreferencesDomain> = state.asStateFlow()

        override suspend fun updateRepeatCount(value: Int) {
            state.update { it.copy(repeatCount = value) }
        }

        override suspend fun updateFocusMinutes(value: Int) {
            state.update { it.copy(focusMinutes = value) }
        }

        override suspend fun updateBreakMinutes(value: Int) {
            state.update { it.copy(breakMinutes = value) }
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

        override suspend fun updateAppTheme(theme: AppTheme) {
            state.update { it.copy(appTheme = theme) }
        }

        override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
            state.update { it.copy(alwaysOnDisplayEnabled = enabled) }
        }

        fun emit(preferences: PreferencesDomain) {
            state.value = preferences
        }
    }

    private class FakeFocusRepository(private var hasActive: Boolean) : ActiveSessionRepository {
        override suspend fun hasActiveSession(): Boolean = hasActive

        override suspend fun getActiveSession(): PomodoroSessionDomain = PomodoroSessionDomain()

        override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
            hasActive = true
        }

        override suspend fun updateActiveSession(snapshot: PomodoroSessionDomain) {
            hasActive = !hasActive
        }

        override suspend fun completeSession(snapshot: PomodoroSessionDomain) {
            hasActive = false
        }

        override suspend fun clearActiveSession() {
            hasActive = false
        }
    }

    private class FakeHistoryRepository : HistorySessionRepository {
        override suspend fun getHistory(year: Int) = DomainResult.Error("not implemented", -1)
        override suspend fun insertHistory(session: PomodoroSessionDomain) = Unit
    }

    @OptIn(ExperimentalTime::class)
    private class FakeCurrentTimeProvider : CurrentTimeProvider {
        override fun now(): Long = 0L

        override fun nowInstant(): Instant = Instant.fromEpochMilliseconds(0)
    }
}
