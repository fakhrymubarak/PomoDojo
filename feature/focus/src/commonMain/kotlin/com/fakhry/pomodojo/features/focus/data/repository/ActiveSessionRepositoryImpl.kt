package com.fakhry.pomodojo.features.focus.data.repository

import com.fakhry.pomodojo.core.datastore.PreferenceStorage
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.features.focus.domain.repository.ActiveSessionRepository
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ActiveSessionRepositoryImpl(
    private val dataStore: PreferenceStorage,
    private val dispatcher: DispatcherProvider,
) : ActiveSessionRepository {

    override suspend fun getActiveSession(): PomodoroSessionDomain = withContext(dispatcher.io) {
        return@withContext dataStore.activeSession.first()
    }

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        withContext(dispatcher.io) {
            dataStore.saveActiveSession(snapshot)
        }
    }

    override suspend fun updateActiveSession(snapshot: PomodoroSessionDomain) {
        saveActiveSession(snapshot)
    }

    override suspend fun completeSession(snapshot: PomodoroSessionDomain) {
        clearActiveSession()
    }

    override suspend fun clearActiveSession() {
        withContext(dispatcher.io) {
            dataStore.clearActiveSession()
        }
    }

    override suspend fun hasActiveSession(): Boolean = withContext(dispatcher.io) {
        return@withContext dataStore.activeSession.first() != PomodoroSessionDomain()
    }
}
