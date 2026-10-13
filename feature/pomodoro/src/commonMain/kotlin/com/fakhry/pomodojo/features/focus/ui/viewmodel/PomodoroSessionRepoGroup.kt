package com.fakhry.pomodojo.features.focus.ui.viewmodel

import com.fakhry.pomodojo.domain.history.repository.HistorySessionRepository
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionStore
import com.fakhry.pomodojo.domain.preferences.repository.PreferencesRepository

data class PomodoroSessionRepoGroup(
    val preferencesRepository: PreferencesRepository,
    val sessionStore: ActiveSessionStore,
    val historyRepository: HistorySessionRepository,
)
