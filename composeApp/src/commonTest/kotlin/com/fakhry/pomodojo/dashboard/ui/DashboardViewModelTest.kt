package com.fakhry.pomodojo.dashboard.ui

import com.fakhry.pomodojo.commons.domain.state.DomainResult
import com.fakhry.pomodojo.core.framework.datetime.CurrentTimeProvider
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.dashboard.domain.model.HistoryDomain
import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.dashboard.ui.viewmodel.DashboardViewModel
import com.fakhry.pomodojo.features.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.features.focus.domain.repository.ActiveSessionRepository
import com.fakhry.pomodojo.features.focus.domain.repository.HistorySessionRepository
import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.features.preferences.domain.usecase.PreferencesRepository
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

        assertEquals("30:00", viewModel.formattedTime.value)
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

        assertEquals("45:00", viewModel.formattedTime.value)
        assertTrue(viewModel.hasActiveSession.value)
    }

    @Test
    fun `fetchHistory with success updates history state`() = runTest(dispatcher) {
        val repository = FakePreferencesRepository(PreferencesDomain())
        val focusRepository = FakeFocusRepository(hasActive = false)
        val historyData = PomodoroHistoryDomain(
            focusMinutesThisYear = 250,
            availableYears = listOf(2024, 2023),
            histories = listOf(
                HistoryDomain(date = "2024-01-15", focusMinutes = 125, breakMinutes = 25),
                HistoryDomain(date = "2024-01-16", focusMinutes = 125, breakMinutes = 25),
            ),
        )
        val historyRepository = FakeHistoryRepository(historyData)
        val currentTimeProvider = FakeCurrentTimeProvider(year = 2024)
        val dispatcherProvider = DispatcherProvider(dispatcher)

        val viewModel =
            DashboardViewModel(
                historyRepo = historyRepository,
                repository = repository,
                focusRepository = focusRepository,
                dispatcher = dispatcherProvider,
                currentTimeProvider = currentTimeProvider,
            )
        advanceUntilIdle()

        val historyState = viewModel.historyState.value
        assertEquals(2024, historyState.selectedYear)
        assertEquals("250", historyState.focusMinutesThisYear)
        assertEquals(listOf(2024, 2023), historyState.availableYears)
        assertTrue(historyState.cells.isNotEmpty())
    }

    @Test
    fun `fetchHistory with error leaves history state empty`() = runTest(dispatcher) {
        val repository = FakePreferencesRepository(PreferencesDomain())
        val focusRepository = FakeFocusRepository(hasActive = false)
        val historyRepository = FakeHistoryRepository(error = true)
        val currentTimeProvider = FakeCurrentTimeProvider()
        val dispatcherProvider = DispatcherProvider(dispatcher)

        val viewModel =
            DashboardViewModel(
                historyRepo = historyRepository,
                repository = repository,
                focusRepository = focusRepository,
                dispatcher = dispatcherProvider,
                currentTimeProvider = currentTimeProvider,
            )
        advanceUntilIdle()

        val historyState = viewModel.historyState.value
        assertEquals(0, historyState.selectedYear)
        assertEquals("0", historyState.focusMinutesThisYear)
        assertTrue(historyState.availableYears.isEmpty())
    }

    @Test
    fun `selectYear updates selected year and refetches history`() = runTest(dispatcher) {
        val repository = FakePreferencesRepository(PreferencesDomain())
        val focusRepository = FakeFocusRepository(hasActive = false)
        val historyData2024 = PomodoroHistoryDomain(
            focusMinutesThisYear = 250,
            availableYears = listOf(2024, 2023),
            histories = listOf(
                HistoryDomain(date = "2024-01-15", focusMinutes = 125, breakMinutes = 25),
            ),
        )
        val historyData2023 = PomodoroHistoryDomain(
            focusMinutesThisYear = 180,
            availableYears = listOf(2024, 2023),
            histories = listOf(
                HistoryDomain(date = "2023-01-15", focusMinutes = 90, breakMinutes = 18),
            ),
        )
        val historyRepository = FakeHistoryRepository(historyData2024, historyData2023)
        val currentTimeProvider = FakeCurrentTimeProvider(year = 2024)
        val dispatcherProvider = DispatcherProvider(dispatcher)

        val viewModel =
            DashboardViewModel(
                historyRepo = historyRepository,
                repository = repository,
                focusRepository = focusRepository,
                dispatcher = dispatcherProvider,
                currentTimeProvider = currentTimeProvider,
            )
        advanceUntilIdle()

        assertEquals(2024, viewModel.historyState.value.selectedYear)
        assertEquals("250", viewModel.historyState.value.focusMinutesThisYear)

        viewModel.selectYear(2023)
        advanceUntilIdle()

        assertEquals(2023, viewModel.historyState.value.selectedYear)
        assertEquals("180", viewModel.historyState.value.focusMinutesThisYear)
    }

    @Test
    fun `selectYear with same year does not refetch`() = runTest(dispatcher) {
        val repository = FakePreferencesRepository(PreferencesDomain())
        val focusRepository = FakeFocusRepository(hasActive = false)
        val historyData = PomodoroHistoryDomain(
            focusMinutesThisYear = 250,
            availableYears = listOf(2024),
            histories = listOf(),
        )
        val historyRepository = FakeHistoryRepository(historyData)
        val currentTimeProvider = FakeCurrentTimeProvider(year = 2024)
        val dispatcherProvider = DispatcherProvider(dispatcher)

        val viewModel =
            DashboardViewModel(
                historyRepo = historyRepository,
                repository = repository,
                focusRepository = focusRepository,
                dispatcher = dispatcherProvider,
                currentTimeProvider = currentTimeProvider,
            )
        advanceUntilIdle()

        val fetchCountBefore = historyRepository.fetchCount
        viewModel.selectYear(2024)
        advanceUntilIdle()

        assertEquals(fetchCountBefore, historyRepository.fetchCount)
    }

    @Test
    fun `selectYear with invalid year does not update state`() = runTest(dispatcher) {
        val repository = FakePreferencesRepository(PreferencesDomain())
        val focusRepository = FakeFocusRepository(hasActive = false)
        val historyData = PomodoroHistoryDomain(
            focusMinutesThisYear = 250,
            availableYears = listOf(2024, 2023),
            histories = listOf(),
        )
        val historyRepository = FakeHistoryRepository(historyData)
        val currentTimeProvider = FakeCurrentTimeProvider(year = 2024)
        val dispatcherProvider = DispatcherProvider(dispatcher)

        val viewModel =
            DashboardViewModel(
                historyRepo = historyRepository,
                repository = repository,
                focusRepository = focusRepository,
                dispatcher = dispatcherProvider,
                currentTimeProvider = currentTimeProvider,
            )
        advanceUntilIdle()

        val stateBefore = viewModel.historyState.value
        viewModel.selectYear(2022) // Not in available years
        advanceUntilIdle()

        assertEquals(stateBefore.selectedYear, viewModel.historyState.value.selectedYear)
        assertEquals(
            stateBefore.focusMinutesThisYear,
            viewModel.historyState.value.focusMinutesThisYear,
        )
    }

    @Test
    fun `checkHasActiveSession updates active session state`() = runTest(dispatcher) {
        val repository = FakePreferencesRepository(PreferencesDomain())
        val focusRepository = FakeFocusRepository(hasActive = false)
        val historyRepository = FakeHistoryRepository()
        val currentTimeProvider = FakeCurrentTimeProvider()
        val dispatcherProvider = DispatcherProvider(dispatcher)

        val viewModel =
            DashboardViewModel(
                historyRepo = historyRepository,
                repository = repository,
                focusRepository = focusRepository,
                dispatcher = dispatcherProvider,
                currentTimeProvider = currentTimeProvider,
            )
        advanceUntilIdle()

        assertFalse(viewModel.hasActiveSession.value)

        focusRepository.setHasActive(true)
        viewModel.checkHasActiveSession()
        advanceUntilIdle()

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

        override suspend fun updateHasActiveSession(value: Boolean) {
            state.update { it.copy(hasActiveSession = value) }
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

        fun setHasActive(value: Boolean) {
            hasActive = value
        }
    }

    private class FakeHistoryRepository(
        private val data2024: PomodoroHistoryDomain? = null,
        private val data2023: PomodoroHistoryDomain? = null,
        private val error: Boolean = false,
    ) : HistorySessionRepository {
        var fetchCount = 0

        override suspend fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain> {
            fetchCount++
            if (error) {
                return DomainResult.Error("not implemented", -1)
            }
            return when (year) {
                2024 -> data2024?.let { DomainResult.Success(it) } ?: DomainResult.Error(
                    "No data",
                    -1,
                )

                2023 -> data2023?.let { DomainResult.Success(it) } ?: DomainResult.Error(
                    "No data",
                    -1,
                )

                else -> DomainResult.Error("not implemented", -1)
            }
        }

        override suspend fun insertHistory(session: PomodoroSessionDomain) = Unit
    }

    @OptIn(ExperimentalTime::class)
    private class FakeCurrentTimeProvider(private val year: Int = 2024) : CurrentTimeProvider {
        override fun now(): Long = 0L

        override fun nowInstant(): Instant {
            // Create an instant that represents January 1st of the given year
            val millis = when (year) {
                2024 -> 1704067200000L // 2024-01-01 00:00:00 UTC
                2023 -> 1672531200000L // 2023-01-01 00:00:00 UTC
                else -> 0L
            }
            return Instant.fromEpochMilliseconds(millis)
        }
    }
}
