package com.fakhry.pomodojo.dashboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.dashboard.ui.mapper.mapToHistorySectionUi
import com.fakhry.pomodojo.dashboard.ui.model.HistorySectionUi
import com.fakhry.pomodojo.focus.domain.repository.ActiveSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.HistorySessionRepository
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.ui.state.DomainResult
import com.fakhry.pomodojo.utils.DispatcherProvider
import com.fakhry.pomodojo.utils.formatTimerMinutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

class DashboardViewModel(
    private val historyRepo: HistorySessionRepository,
    private val repository: PreferencesRepository,
    private val focusRepository: ActiveSessionRepository,
    private val dispatcher: DispatcherProvider,
    private val currentTimeProvider: CurrentTimeProvider,
) : ViewModel() {
    private val _hasActiveSession = MutableStateFlow(false)
    val hasActiveSession: StateFlow<Boolean> = _hasActiveSession.asStateFlow()

    private val _formattedTime = MutableStateFlow("")
    val formattedTime = _formattedTime.asStateFlow()

    private val _historyState = MutableStateFlow(HistorySectionUi())
    val historyState: StateFlow<HistorySectionUi> = _historyState.asStateFlow()

    init {
        checkHasActiveSession()
        fetchPreferences()
        fetchHistory()
    }

    fun checkHasActiveSession() = viewModelScope.launch(dispatcher.io) {
        val hasActiveSession = focusRepository.hasActiveSession()
        _hasActiveSession.update { hasActiveSession }
    }

    fun fetchPreferences() = viewModelScope.launch(dispatcher.io) {
        repository.preferences.collect { preferences ->
            _formattedTime.update { formatTimerMinutes(preferences.focusMinutes) }
        }
    }

    fun fetchHistory(selectedYear: Int = -1) = viewModelScope.launch(dispatcher.io) {
        @OptIn(ExperimentalTime::class)
        val now = currentTimeProvider.nowInstant().toLocalDateTime(TimeZone.UTC)
        val today = now.date
        val currentYear =
            if (selectedYear < 0) {
                today.year
            } else {
                selectedYear
            }
        when (val result = historyRepo.getHistory(currentYear)) {
            is DomainResult.Success -> {
                val historySectionUi: HistorySectionUi =
                    result.data.mapToHistorySectionUi(
                        selectedYear = currentYear,
                        currentDate = today,
                    )
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
