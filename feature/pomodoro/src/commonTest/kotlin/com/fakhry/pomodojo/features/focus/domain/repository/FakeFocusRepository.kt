package com.fakhry.pomodojo.features.focus.domain.repository

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionRepository

class FakeFocusRepository(private var hasActive: Boolean) : ActiveSessionRepository {
    private var session: PomodoroSessionDomain? = if (hasActive) PomodoroSessionDomain() else null

    override suspend fun hasActiveSession(): Boolean = hasActive

    override suspend fun getActiveSession(): PomodoroSessionDomain =
        session ?: PomodoroSessionDomain()

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        hasActive = true
        session = snapshot
    }

    override suspend fun clearActiveSession() {
        hasActive = false
        session = null
    }

    fun setHasActive(value: Boolean) {
        hasActive = value
        session = if (value) PomodoroSessionDomain() else null
    }
}
