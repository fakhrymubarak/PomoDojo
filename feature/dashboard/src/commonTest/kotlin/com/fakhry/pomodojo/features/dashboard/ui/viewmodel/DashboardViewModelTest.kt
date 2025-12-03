package com.fakhry.pomodojo.features.dashboard.ui.viewmodel

import com.fakhry.pomodojo.core.notification.datetime.FakeCurrentTimeProvider
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.domain.common.DomainResult
import com.fakhry.pomodojo.domain.history.model.HistoryDomain
import com.fakhry.pomodojo.domain.history.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.domain.history.repository.HistorySessionRepository
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionRepository
import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences
import com.fakhry.pomodojo.domain.preferences.repository.PreferencesRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val history2024 = PomodoroHistoryDomain(
        focusMinutesThisYear = 250,
        availableYears = listOf(2024, 2023),
        histories = listOf(
            HistoryDomain(date = "2024-01-03", focusMinutes = 60, breakMinutes = 15),
            HistoryDomain(date = "2024-02-14", focusMinutes = 50, breakMinutes = 10),
        ),
    )
    private val history2023 = PomodoroHistoryDomain(
        focusMinutesThisYear = 180,
        availableYears = listOf(2024, 2023),
        histories = listOf(
            HistoryDomain(date = "2023-01-15", focusMinutes = 45, breakMinutes = 15),
            HistoryDomain(date = "2023-03-07", focusMinutes = 35, breakMinutes = 5),
        ),
    )
    private val repository = FakePreferencesRepository(PomodoroPreferences(focusMinutes = 30))
    private val focusRepository = FakeFocusRepository(hasActive = false)
    private val historyRepository = FakeHistoryRepository(
        data2024 = history2024,
        data2023 = history2023,
    )
    private val dispatcherProvider = DispatcherProvider(dispatcher)
    private val currentTimeProvider = FakeCurrentTimeProvider()

    private val viewModel = DashboardViewModel(
        historyRepo = historyRepository,
        repository = repository,
        focusRepository = focusRepository,
        dispatcher = dispatcherProvider,
        currentTimeProvider = currentTimeProvider,
    )

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
        advanceUntilIdle()

        assertEquals("30:00", viewModel.formattedTime.value)
        assertFalse(viewModel.hasActiveSession.value)
    }

    @Test
    fun `pref state updates when repository emits new value`() = runTest(dispatcher) {
        advanceUntilIdle()

        repository.emit(
            repository.current.copy(
                focusMinutes = 45,
            ),
        )
        advanceUntilIdle()

        assertEquals("45:00", viewModel.formattedTime.value)
    }

    @Test
    fun `fetchHistory with success updates history state`() = runTest(dispatcher) {
        val viewModel = DashboardViewModel(
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
        val errorRepository = FakeHistoryRepository(error = true)
        val viewModel = DashboardViewModel(
            historyRepo = errorRepository,
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
        val viewModel = DashboardViewModel(
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
        advanceUntilIdle()

        val fetchCountBefore = historyRepository.fetchCount
        viewModel.selectYear(2024)
        advanceUntilIdle()

        assertEquals(fetchCountBefore, historyRepository.fetchCount)
    }

    @Test
    fun `selectYear with invalid year does not update state`() = runTest(dispatcher) {
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
        advanceUntilIdle()

        assertFalse(viewModel.hasActiveSession.value)

        focusRepository.setHasActive(true)
        viewModel.checkHasActiveSession()
        advanceUntilIdle()

        assertTrue(viewModel.hasActiveSession.value)
    }
}

private class FakeFocusRepository(private var hasActive: Boolean) : ActiveSessionRepository {
    private var session: PomodoroSessionDomain? = if (hasActive) PomodoroSessionDomain() else null

    override suspend fun hasActiveSession(): Boolean = hasActive

    override suspend fun getActiveSession(): PomodoroSessionDomain =
        session ?: PomodoroSessionDomain()

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        session = snapshot
        hasActive = true
    }

    override suspend fun clearActiveSession() {
        session = null
        hasActive = false
    }

    fun setHasActive(value: Boolean) {
        hasActive = value
        session = if (value) PomodoroSessionDomain() else null
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
        if (error) return DomainResult.Error("not implemented", -1)
        return when (year) {
            2024 -> data2024?.let { DomainResult.Success(it) } ?: DomainResult.Error("No data", -1)
            2023 -> data2023?.let { DomainResult.Success(it) } ?: DomainResult.Error("No data", -1)
            else -> DomainResult.Error("not implemented", -1)
        }
    }

    override suspend fun insertHistory(session: PomodoroSessionDomain) = Unit
}

private class FakePreferencesRepository(
    initial: PomodoroPreferences = PomodoroPreferences(),
) : PreferencesRepository {
    private val pomodoroState = MutableStateFlow(initial)
    val current: PomodoroPreferences get() = pomodoroState.value

    override val preferences: Flow<PomodoroPreferences> = pomodoroState.asStateFlow()

    override suspend fun updateRepeatCount(value: Int) {
        pomodoroState.update { it.copy(repeatCount = value) }
    }

    override suspend fun updateFocusMinutes(value: Int) {
        pomodoroState.update { it.copy(focusMinutes = value) }
    }

    override suspend fun updateBreakMinutes(value: Int) {
        pomodoroState.update { it.copy(breakMinutes = value) }
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        pomodoroState.update { it.copy(longBreakEnabled = enabled) }
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        pomodoroState.update { it.copy(longBreakAfter = value) }
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        pomodoroState.update { it.copy(longBreakMinutes = value) }
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        pomodoroState.update { it.copy(alwaysOnDisplayEnabled = enabled) }
    }

    fun emit(preferences: PomodoroPreferences) {
        pomodoroState.value = preferences
    }
}
