package com.fakhry.pomodojo.focus.domain.repository

import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain

/**
 * Abstraction over the storage of active pomodoro sessions.
 */
interface PomodoroSessionRepository {
    suspend fun getActiveSession(): PomodoroSessionDomain

    suspend fun saveActiveSession(snapshot: PomodoroSessionDomain)

    suspend fun updateActiveSession(snapshot: PomodoroSessionDomain)

    suspend fun completeSession(snapshot: PomodoroSessionDomain)

    suspend fun clearActiveSession()

    suspend fun hasActiveSession(): Boolean
}
