package com.fakhry.pomodojo.preferences.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.preferences.ui.mapper.mapToUiModel
import com.fakhry.pomodojo.preferences.ui.model.PreferencesUiModel
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val repository: PreferencesRepository,
    private val timelineBuilder: BuildTimerSegmentsUseCase,
    private val hourSplitter: BuildHourSplitTimelineUseCase,
    private val dispatcher: DispatcherProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(PreferencesUiModel())
    val state: StateFlow<PreferencesUiModel> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher.io) {
            repository.preferences.collect { preferences ->
                _state.value = preferences.mapToUiModel(
                    timelineBuilder = {
                        timelineBuilder(0L, it).toCompletedTimeline()
                    },
                    hourSplitter = hourSplitter::invoke,
                )
            }
        }
    }

    fun onRepeatCountChanged(count: Int) {
        viewModelScope.launch(dispatcher.io) {
            repository.updateRepeatCount(count)
        }
    }

    fun onFocusOptionSelected(minutes: Int) {
        viewModelScope.launch(dispatcher.io) {
            repository.updateFocusMinutes(minutes)
        }
    }

    fun onBreakOptionSelected(minutes: Int) {
        viewModelScope.launch(dispatcher.io) {
            repository.updateBreakMinutes(minutes)
        }
    }

    fun onLongBreakEnabledToggled(enabled: Boolean) {
        if (_state.value.isLongBreakEnabled == enabled) return

        viewModelScope.launch(dispatcher.io) {
            repository.updateLongBreakEnabled(enabled)
        }
    }

    fun onLongBreakAfterSelected(count: Int) {
        viewModelScope.launch(dispatcher.io) {
            repository.updateLongBreakAfter(count)
        }
    }

    fun onLongBreakMinutesSelected(minutes: Int) {
        viewModelScope.launch(dispatcher.io) {
            repository.updateLongBreakMinutes(minutes)
        }
    }

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch(dispatcher.io) {
            repository.updateAppTheme(theme)
        }
    }

    private fun List<TimerSegmentsDomain>.toCompletedTimeline() = map { segment ->
        segment.copy(timerStatus = TimerStatusDomain.Completed)
    }

    override fun onCleared() {
        println("PreferencesViewModel onCleared")
        super.onCleared()
    }

}
