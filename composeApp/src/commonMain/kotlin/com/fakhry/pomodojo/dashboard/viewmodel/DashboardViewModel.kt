package com.fakhry.pomodojo.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.dashboard.model.HistorySectionUi
import com.fakhry.pomodojo.dashboard.model.previewDashboardState
import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: PreferencesRepository,
) : ViewModel() {

    private val _prefState = MutableStateFlow(PreferencesDomain())
    val prefState: StateFlow<PreferencesDomain> = _prefState.asStateFlow()

    private val _historyState = MutableStateFlow(previewDashboardState.historySection)
    val historyState: StateFlow<HistorySectionUi> = _historyState.asStateFlow()

    init {
        viewModelScope.launch {
            async { fetchPreferences() }
            async { fetchHistory() }
        }
    }

    suspend fun fetchPreferences() {
        repository.preferences.collect { preferences ->
            _prefState.value = preferences
        }
    }

    fun fetchHistory() {
        // Fill fetch history from DB logics
    }

    fun selectYear(year: Int) {
        _historyState.update { current ->
            if (year == current.selectedYear || year !in current.availableYears) {
                current
            } else {
                current.copy(selectedYear = year)
            }
        }
    }
}
