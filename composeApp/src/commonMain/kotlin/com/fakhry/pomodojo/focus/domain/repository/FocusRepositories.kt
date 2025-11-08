package com.fakhry.pomodojo.focus.domain.repository

import com.fakhry.pomodojo.focus.domain.model.ActiveFocusSessionDomain

/**
 * Abstraction over the storage of active pomodoro sessions.
 */
interface PomodoroSessionRepository {
    suspend fun getActiveSession(): ActiveFocusSessionDomain
    suspend fun saveActiveSession(snapshot: ActiveFocusSessionDomain)
    suspend fun updateActiveSession(snapshot: ActiveFocusSessionDomain)
    suspend fun completeSession(snapshot: ActiveFocusSessionDomain)
    suspend fun clearActiveSession()
    suspend fun hasActiveSession(): Boolean
}

