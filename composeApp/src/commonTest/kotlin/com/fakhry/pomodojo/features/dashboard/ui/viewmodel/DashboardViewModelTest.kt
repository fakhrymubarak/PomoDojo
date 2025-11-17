package com.fakhry.pomodojo.features.dashboard.ui.viewmodel

import com.fakhry.pomodojo.core.framework.datetime.FakeCurrentTimeProvider
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.features.focus.domain.repository.FakeFocusRepository
import com.fakhry.pomodojo.features.focus.domain.repository.FakeHistoryRepository
import com.fakhry.pomodojo.features.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.features.preferences.domain.usecase.FakePreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val repository = FakePreferencesRepository(PreferencesDomain(focusMinutes = 30))
    val focusRepository = FakeFocusRepository(hasActive = false)
    val historyRepository = FakeHistoryRepository()
    val dispatcherProvider = DispatcherProvider(dispatcher)
    val currentTimeProvider = FakeCurrentTimeProvider()

    val viewModel = DashboardViewModel(
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

        repository.emit(repository.current.copy(focusMinutes = 45))
        advanceUntilIdle()

        assertEquals("45:00", viewModel.formattedTime.value)
        assertTrue(viewModel.hasActiveSession.value)
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

