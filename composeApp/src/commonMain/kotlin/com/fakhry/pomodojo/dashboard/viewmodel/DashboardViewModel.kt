package com.fakhry.pomodojo.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.dashboard.model.HistorySectionUi
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val message: String, val code: Int) : DomainResult<Nothing>()
}

data class HistoryDomain(
    val date: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
)

data class PomodoroHistoryDomain(
    val focusMinutesThisYear: Int,
    val histories: List<HistoryDomain>,

)

interface PomodoroHistoryRepository {
    fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain>
}

class PomodoroHistoryRepositoryImpl : PomodoroHistoryRepository {
    override fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain> {
        val histories = listOf(
            HistoryDomain("01-01-2023", 100, 20),
            HistoryDomain("31-01-2024", 50, 10),
            HistoryDomain("10-11-2025", 200, 40),
        )
        val focusMinutesThisYear = 200
        return DomainResult.Success(PomodoroHistoryDomain(focusMinutesThisYear, histories))
    }
}

class DashboardViewModel(
    private val historyRepo: PomodoroHistoryRepository,
    private val repository: PreferencesRepository,
    private val focusRepository: PomodoroSessionRepository,
    private val dispatcher: DispatcherProvider,
    private val currentTimeProvider: CurrentTimeProvider,
) : ViewModel() {
    private val _hasActiveSession = MutableStateFlow(false)
    val hasActiveSession: StateFlow<Boolean> = _hasActiveSession.asStateFlow()

    private val _prefState = MutableStateFlow(PreferencesDomain())
    val prefState: StateFlow<PreferencesDomain> = _prefState.asStateFlow()

    private val _historyState = MutableStateFlow(HistorySectionUi())
    val historyState: StateFlow<HistorySectionUi> = _historyState.asStateFlow()

    init {
        viewModelScope.launch {
            async { checkHasActiveSession() }
            async { fetchPreferences() }
            async { fetchHistory() }
        }
    }

    suspend fun checkHasActiveSession() {
        val hasActiveSession = focusRepository.hasActiveSession()
        _hasActiveSession.value = hasActiveSession
    }

    suspend fun fetchPreferences() {
        repository.preferences.collect { preferences ->
            _prefState.value = preferences
        }
    }

    fun fetchHistory(selectedYear: Int = -1) = viewModelScope.launch(dispatcher.io) {
        @OptIn(ExperimentalTime::class)
        val currentYear = if (selectedYear < 0) {
            currentTimeProvider.nowInstant()
                .toLocalDateTime(TimeZone.UTC)
                .year
        } else {
            selectedYear
        }
        when (val result = historyRepo.getHistory(currentYear)) {
            is DomainResult.Success -> {
                val historySectionUi: HistorySectionUi = result.data.mapToHistorySectionUi()
                _historyState.update { historySectionUi }
            }

            is DomainResult.Error -> {}
        }
    }

    fun selectYear(year: Int) {
        _historyState.update { current ->
            if (year == current.selectedYear || year !in current.availableYears) {
                current
            } else {
                fetchHistory(year)
                current.copy(selectedYear = year)
            }
        }
    }
}
