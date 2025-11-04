package com.fakhry.pomodojo.preferences.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.domain.AppTheme
import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import com.fakhry.pomodojo.preferences.domain.PreferencesValidator
import com.fakhry.pomodojo.preferences.domain.TimelinePreviewBuilder
import com.fakhry.pomodojo.preferences.ui.state.PreferenceOption
import com.fakhry.pomodojo.preferences.ui.state.PreferencesState
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val repository: PreferencesRepository,
    private val timelineBuilder: TimelinePreviewBuilder,
    private val dispatcher: DispatcherProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(PreferencesState())
    val state: StateFlow<PreferencesState> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher.io) {
            repository.preferences.collect { preferences ->
                _state.value = mapToState(preferences)
            }
        }
    }

    fun onRepeatCountChanged(count: Int) {
        if (!PreferencesValidator.isValidRepeatCount(count) ||
            count == _state.value.repeatCount
        ) return

        viewModelScope.launch(dispatcher.io) {
            repository.updateRepeatCount(count)
        }
    }

    fun onFocusOptionSelected(minutes: Int) {
        if (!PreferencesValidator.isValidFocusMinutes(minutes) ||
            _state.value.focusOptions.firstOrNull { it.selected }?.value == minutes
        ) return

        viewModelScope.launch(dispatcher.io) {
            repository.updateFocusMinutes(minutes)
        }
    }

    fun onBreakOptionSelected(minutes: Int) {
        if (!PreferencesValidator.isValidBreakMinutes(minutes) ||
            _state.value.breakOptions.firstOrNull { it.selected }?.value == minutes
        ) return

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
        if (!PreferencesValidator.isValidLongBreakAfter(count) ||
            _state.value.longBreakAfterOptions.firstOrNull { it.selected }?.value == count
        ) return

        viewModelScope.launch(dispatcher.io) {
            repository.updateLongBreakAfter(count)
        }
    }

    fun onLongBreakMinutesSelected(minutes: Int) {
        if (!PreferencesValidator.isValidLongBreakMinutes(minutes) ||
            _state.value.longBreakOptions.firstOrNull { it.selected }?.value == minutes
        ) return

        viewModelScope.launch(dispatcher.io) {
            repository.updateLongBreakMinutes(minutes)
        }
    }

    fun onThemeSelected(theme: AppTheme) {
        if (_state.value.selectedTheme == theme) return

        viewModelScope.launch(dispatcher.io) {
            repository.updateAppTheme(theme)
        }
    }

    private fun mapToState(preferences: PomodoroPreferences): PreferencesState {
        val longBreakEnabled = preferences.longBreakEnabled
        val themeOptions = AppTheme.entries.map { theme ->
            PreferenceOption(
                label = theme.displayName,
                value = theme,
                selected = preferences.appTheme == theme,
            )
        }.toPersistentList()

        return PreferencesState(
            selectedTheme = preferences.appTheme,
            themeOptions = themeOptions,
            repeatCount = preferences.repeatCount,
            focusOptions = FOCUS_OPTIONS.map { minutes ->
                PreferenceOption(
                    label = "$minutes mins",
                    value = minutes,
                    selected = preferences.focusMinutes == minutes,
                )
            }.toPersistentList(),
            breakOptions = BREAK_OPTIONS.map { minutes ->
                PreferenceOption(
                    label = "$minutes mins",
                    value = minutes,
                    selected = preferences.breakMinutes == minutes,
                )
            }.toPersistentList(),
            isLongBreakEnabled = longBreakEnabled,
            longBreakAfterOptions = LONG_BREAK_AFTER.map { count ->
                PreferenceOption(
                    label = "$count focuses",
                    value = count,
                    selected = preferences.longBreakAfter == count,
                    enabled = longBreakEnabled,
                )
            }.toPersistentList(),
            longBreakOptions = LONG_BREAK_MINUTES.map { minutes ->
                PreferenceOption(
                    label = "$minutes mins",
                    value = minutes,
                    selected = preferences.longBreakMinutes == minutes,
                    enabled = longBreakEnabled,
                )
            }.toPersistentList(),
            timelineSegments = timelineBuilder.build(preferences),
            isLoading = false,
        )
    }


    override fun onCleared() {
        println("PreferencesViewModel onCleared")
        super.onCleared()
    }

    companion object {
        private val FOCUS_OPTIONS = listOf(10, 25, 50)
        private val BREAK_OPTIONS = listOf(2, 5, 10)
        private val LONG_BREAK_AFTER = listOf(6, 4, 2)
        private val LONG_BREAK_MINUTES = listOf(4, 10, 20)
    }
}
