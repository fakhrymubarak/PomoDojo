package com.fakhry.pomodojo.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val repository: PreferencesRepository,
    private val timelineBuilder: TimelinePreviewBuilder,
    scope: CoroutineScope? = null,
) : ViewModel() {

    private val coroutineScope = scope ?: viewModelScope

    private val _state = MutableStateFlow(PreferencesState())
    val state: StateFlow<PreferencesState> = _state.asStateFlow()

    init {
        coroutineScope.launch {
            repository.preferences.collect { preferences ->
                _state.value = mapToState(preferences)
            }
        }
    }

    fun onRepeatCountChanged(count: Int) {
        if (!PreferencesValidator.isValidRepeatCount(count) ||
            count == _state.value.repeatCount
        ) return

        coroutineScope.launch {
            repository.updateRepeatCount(count)
        }
    }

    fun onFocusOptionSelected(minutes: Int) {
        if (!PreferencesValidator.isValidFocusMinutes(minutes) ||
            _state.value.focusOptions.firstOrNull { it.selected }?.value == minutes
        ) return

        coroutineScope.launch {
            repository.updateFocusMinutes(minutes)
        }
    }

    fun onBreakOptionSelected(minutes: Int) {
        if (!PreferencesValidator.isValidBreakMinutes(minutes) ||
            _state.value.breakOptions.firstOrNull { it.selected }?.value == minutes
        ) return

        coroutineScope.launch {
            repository.updateBreakMinutes(minutes)
        }
    }

    fun onLongBreakEnabledToggled(enabled: Boolean) {
        if (_state.value.isLongBreakEnabled == enabled) return

        coroutineScope.launch {
            repository.updateLongBreakEnabled(enabled)
        }
    }

    fun onLongBreakAfterSelected(count: Int) {
        if (!PreferencesValidator.isValidLongBreakAfter(count) ||
            _state.value.longBreakAfterOptions.firstOrNull { it.selected }?.value == count
        ) return

        coroutineScope.launch {
            repository.updateLongBreakAfter(count)
        }
    }

    fun onLongBreakMinutesSelected(minutes: Int) {
        if (!PreferencesValidator.isValidLongBreakMinutes(minutes) ||
            _state.value.longBreakOptions.firstOrNull { it.selected }?.value == minutes
        ) return

        coroutineScope.launch {
            repository.updateLongBreakMinutes(minutes)
        }
    }

    private fun mapToState(preferences: PomodoroPreferences): PreferencesState {
        val longBreakEnabled = preferences.longBreakEnabled

        return PreferencesState(
            repeatCount = preferences.repeatCount,
            focusOptions = FOCUS_OPTIONS.map { minutes ->
                PreferenceOption(
                    label = "$minutes mins",
                    value = minutes,
                    selected = preferences.focusMinutes == minutes,
                )
            },
            breakOptions = BREAK_OPTIONS.map { minutes ->
                PreferenceOption(
                    label = "$minutes mins",
                    value = minutes,
                    selected = preferences.breakMinutes == minutes,
                )
            },
            isLongBreakEnabled = longBreakEnabled,
            longBreakAfterOptions = LONG_BREAK_AFTER.map { count ->
                PreferenceOption(
                    label = "$count focuses",
                    value = count,
                    selected = preferences.longBreakAfter == count,
                    enabled = longBreakEnabled,
                )
            },
            longBreakOptions = LONG_BREAK_MINUTES.map { minutes ->
                PreferenceOption(
                    label = "$minutes mins",
                    value = minutes,
                    selected = preferences.longBreakMinutes == minutes,
                    enabled = longBreakEnabled,
                )
            },
            timelineSegments = timelineBuilder.build(preferences),
            isLoading = false,
        )
    }

    companion object {
        private val FOCUS_OPTIONS = listOf(10, 25, 50)
        private val BREAK_OPTIONS = listOf(2, 5, 10)
        private val LONG_BREAK_AFTER = listOf(6, 4, 2)
        private val LONG_BREAK_MINUTES = listOf(4, 10, 20)
    }
}
