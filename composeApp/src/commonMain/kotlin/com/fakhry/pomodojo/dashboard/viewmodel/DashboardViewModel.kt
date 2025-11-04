package com.fakhry.pomodojo.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.dashboard.model.DashboardState
import com.fakhry.pomodojo.dashboard.model.previewDashboardState
import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val initialState = DashboardState(
        timerMinutes = PomodoroPreferences.DEFAULT_FOCUS_MINUTES,
        focusMinutesThisYear = 189,
        selectedYear = 2025,
        availableYears = persistentListOf(2025, 2024),
        cells = previewDashboardState.cells,
    )

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.preferences.collect { preferences ->
                _state.update { current ->
                    if (current.timerMinutes == preferences.focusMinutes) {
                        current
                    } else {
                        current.copy(timerMinutes = preferences.focusMinutes)
                    }
                }
            }
        }
    }

    fun selectYear(year: Int) {
        _state.update { current ->
            if (year == current.selectedYear || year !in current.availableYears) {
                current
            } else {
                current.copy(selectedYear = year)
            }
        }
    }
}
