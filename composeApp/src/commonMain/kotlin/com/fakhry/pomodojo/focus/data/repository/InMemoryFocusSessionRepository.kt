package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.focus.domain.model.FocusSessionSnapshot
import com.fakhry.pomodojo.focus.domain.repository.FocusSessionRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryFocusSessionRepository : FocusSessionRepository {

    private val mutex = Mutex()
    private var activeSession: FocusSessionSnapshot? = null
    private val completedSessions = mutableListOf<FocusSessionSnapshot>()

    override suspend fun getActiveSession(): FocusSessionSnapshot? = mutex.withLock {
        activeSession
    }

    override suspend fun saveActiveSession(snapshot: FocusSessionSnapshot) {
        mutex.withLock {
            activeSession = snapshot
        }
    }

    override suspend fun updateActiveSession(snapshot: FocusSessionSnapshot) {
        mutex.withLock {
            activeSession = snapshot
        }
    }

    override suspend fun completeSession(snapshot: FocusSessionSnapshot) {
        mutex.withLock {
            completedSessions += snapshot
        }
    }

    override suspend fun clearActiveSession() {
        mutex.withLock {
            activeSession = null
        }
    }
}