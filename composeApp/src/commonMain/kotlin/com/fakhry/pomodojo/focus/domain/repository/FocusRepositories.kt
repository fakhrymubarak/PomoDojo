package com.fakhry.pomodojo.focus.domain.repository

import com.fakhry.pomodojo.focus.domain.model.FocusSessionSnapshot

/**
 * Abstraction over the storage of active pomodoro sessions.
 */
interface FocusSessionRepository {
    suspend fun getActiveSession(): FocusSessionSnapshot?
    suspend fun saveActiveSession(snapshot: FocusSessionSnapshot)
    suspend fun updateActiveSession(snapshot: FocusSessionSnapshot)
    suspend fun completeSession(snapshot: FocusSessionSnapshot)
    suspend fun clearActiveSession()
}

