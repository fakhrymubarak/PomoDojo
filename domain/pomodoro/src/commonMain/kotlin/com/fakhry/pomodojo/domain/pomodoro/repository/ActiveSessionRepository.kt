package com.fakhry.pomodojo.domain.pomodoro.repository

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain

/**
 * Abstraction over the storage of active pomodoro sessions.
 */
interface ActiveSessionRepository {
    suspend fun getActiveSession(): PomodoroSessionDomain

    suspend fun saveActiveSession(snapshot: PomodoroSessionDomain)

    suspend fun clearActiveSession()

    suspend fun hasActiveSession(): Boolean
}
