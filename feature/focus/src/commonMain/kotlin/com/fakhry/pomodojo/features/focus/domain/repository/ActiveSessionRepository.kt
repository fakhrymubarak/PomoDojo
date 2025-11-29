package com.fakhry.pomodojo.features.focus.domain.repository

import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain

/**
 * Abstraction over the storage of active pomodoro sessions.
 */
interface ActiveSessionRepository {
    suspend fun getActiveSession(): PomodoroSessionDomain

    suspend fun saveActiveSession(snapshot: PomodoroSessionDomain)

    suspend fun updateActiveSession(snapshot: PomodoroSessionDomain)

    suspend fun completeSession(snapshot: PomodoroSessionDomain)

    suspend fun clearActiveSession()

    suspend fun hasActiveSession(): Boolean
}
