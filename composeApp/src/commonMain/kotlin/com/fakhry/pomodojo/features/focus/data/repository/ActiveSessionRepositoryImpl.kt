package com.fakhry.pomodojo.features.focus.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.features.focus.data.mapper.toData
import com.fakhry.pomodojo.features.focus.data.mapper.toDomain
import com.fakhry.pomodojo.features.focus.data.model.PomodoroSessionData
import com.fakhry.pomodojo.features.focus.domain.repository.ActiveSessionRepository
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ActiveSessionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: DispatcherProvider,
    private val json: Json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        },
) : ActiveSessionRepository {

    override suspend fun getActiveSession(): PomodoroSessionDomain = withContext(dispatcher.io) {
        val snapshot =
            dataStore.data.first()[ACTIVE_SESSION_KEY]
                ?: throw IllegalStateException("No active session stored in database.")
        return@withContext json.decodeFromString<PomodoroSessionData>(snapshot).toDomain()
    }

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        withContext(dispatcher.io) {
            val encoded = json.encodeToString(snapshot.toData())
            dataStore.edit { prefs ->
                prefs[ACTIVE_SESSION_KEY] = encoded
            }
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
            dataStore.edit { prefs ->
                prefs.remove(ACTIVE_SESSION_KEY)
            }
        }
    }

    override suspend fun hasActiveSession(): Boolean = withContext(dispatcher.io) {
        dataStore.data.first()[ACTIVE_SESSION_KEY] != null
    }

    private companion object {
        val ACTIVE_SESSION_KEY = stringPreferencesKey("active_session_snapshot")
    }
}
