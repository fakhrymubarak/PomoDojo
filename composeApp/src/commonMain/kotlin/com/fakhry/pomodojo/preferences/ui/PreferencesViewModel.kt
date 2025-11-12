package com.fakhry.pomodojo.preferences.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.preferences.ui.mapper.DEFAULT_REPEAT_RANGE
import com.fakhry.pomodojo.preferences.ui.mapper.mapToUiModel
import com.fakhry.pomodojo.preferences.ui.model.PreferencesAppearanceUiState
import com.fakhry.pomodojo.preferences.ui.model.PreferencesConfigUiState
import com.fakhry.pomodojo.preferences.ui.model.PreferencesUiModel
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import com.fakhry.pomodojo.preferences.ui.model.toAppearanceUiState
import com.fakhry.pomodojo.preferences.ui.model.toConfigUiState
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val repository: PreferencesRepository,
    private val timelineBuilder: BuildTimerSegmentsUseCase,
    private val hourSplitter: BuildHourSplitTimelineUseCase,
    private val dispatcher: DispatcherProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(PreferencesUiModel())
    val state: StateFlow<PreferencesUiModel> = _state.asStateFlow()
    val isLoadingState: StateFlow<Boolean> = state.map { it.isLoading }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val timelineState: StateFlow<TimelineUiModel> = state.map { it.timeline }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, TimelineUiModel())
    val configState: StateFlow<PreferencesConfigUiState> =
        state.map { it.toConfigUiState() }.distinctUntilChanged().stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PreferencesConfigUiState(
                repeatCount = PreferencesDomain.DEFAULT_REPEAT_COUNT,
                repeatRange = DEFAULT_REPEAT_RANGE,
                focusOptions = persistentListOf(),
                breakOptions = persistentListOf(),
                isLongBreakEnabled = false,
                longBreakAfterOptions = persistentListOf(),
                longBreakOptions = persistentListOf(),
            ),
        )
    val appearanceState: StateFlow<PreferencesAppearanceUiState> =
        state.map { it.toAppearanceUiState() }.distinctUntilChanged().stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PreferencesAppearanceUiState(
                themeOptions = persistentListOf(),
                isAlwaysOnDisplayEnabled = false,
            ),
        )

    init {
        viewModelScope.launch(dispatcher.io) {
            repository.preferences.collect { preferences ->
                val mapped = preferences.mapToUiModel(
                    timelineBuilder = {
                        timelineBuilder(0L, it).toCompletedTimeline()
                    },
                    hourSplitter = hourSplitter::invoke,
                )
                _state.update { current ->
                    if (current.isLoading) {
                        mapped
                    } else {
                        mapped.reuseStableListsFrom(current)
                    }
                }
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

    fun onAlwaysOnDisplayToggled(enabled: Boolean) {
        if (_state.value.isAlwaysOnDisplayEnabled == enabled) return
        viewModelScope.launch(dispatcher.io) {
            repository.updateAlwaysOnDisplayEnabled(enabled)
        }
    }

    private fun List<TimerSegmentsDomain>.toCompletedTimeline() = map { segment ->
        segment.copy(timerStatus = TimerStatusDomain.COMPLETED)
    }

    override fun onCleared() {
        println("PreferencesViewModel onCleared")
        super.onCleared()
    }
}

private fun PreferencesUiModel.reuseStableListsFrom(
    previous: PreferencesUiModel,
): PreferencesUiModel {
    var updatedTimeline = timeline
    if (timeline != previous.timeline) {
        updatedTimeline = timeline.reuseStableListsFrom(previous.timeline)
    }
    return copy(
        themeOptions = themeOptions.reuseIfEqual(previous.themeOptions),
        focusOptions = focusOptions.reuseIfEqual(previous.focusOptions),
        breakOptions = breakOptions.reuseIfEqual(previous.breakOptions),
        longBreakAfterOptions = longBreakAfterOptions.reuseIfEqual(previous.longBreakAfterOptions),
        longBreakOptions = longBreakOptions.reuseIfEqual(previous.longBreakOptions),
        timeline = updatedTimeline,
    )
}

private fun TimelineUiModel.reuseStableListsFrom(previous: TimelineUiModel): TimelineUiModel {
    val segmentsRef = segments.reuseIfEqual(previous.segments)
    val hoursRef = hourSplits.reuseIfEqual(previous.hourSplits)
    return if (segmentsRef === segments && hoursRef === hourSplits) {
        this
    } else {
        copy(segments = segmentsRef, hourSplits = hoursRef)
    }
}

private fun <T> ImmutableList<T>.reuseIfEqual(previous: ImmutableList<T>): ImmutableList<T> =
    if (this == previous) {
        previous
    } else {
        this
    }
