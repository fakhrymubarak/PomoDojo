package com.fakhry.pomodojo.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import com.fakhry.pomodojo.dashboard.model.DashboardState
import com.fakhry.pomodojo.dashboard.model.previewDashboardState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DashboardViewModel : ViewModel() {

    private val initialState = DashboardState(
        timerMinutes = 25,
        focusMinutesThisYear = 189,
        selectedYear = 2025,
        availableYears = persistentListOf(2025, 2024),
        cells = previewDashboardState.cells,
    )

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

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
