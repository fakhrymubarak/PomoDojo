package com.fakhry.pomodojo.features.focus.domain.repository

import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain

class FakeFocusRepository(private var hasActive: Boolean) : ActiveSessionRepository {
    override suspend fun hasActiveSession(): Boolean = hasActive

    override suspend fun getActiveSession(): PomodoroSessionDomain = PomodoroSessionDomain()

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        hasActive = true
    }

    override suspend fun updateActiveSession(snapshot: PomodoroSessionDomain) {
        hasActive = !hasActive
    }

    override suspend fun completeSession(snapshot: PomodoroSessionDomain) {
        hasActive = false
    }

    override suspend fun clearActiveSession() {
        hasActive = false
    }

    fun setHasActive(value: Boolean) {
        hasActive = value
    }
}
