package com.fakhry.pomodojo.data.pomodoro.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.fakhry.pomodojo.core.datastore.PreferenceKeys
import com.fakhry.pomodojo.data.pomodoro.mapper.toDataStore
import com.fakhry.pomodojo.data.pomodoro.mapper.toPomodoroSession
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ActiveSessionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : ActiveSessionRepository {
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun getActiveSession(): PomodoroSessionDomain = dataStore.data.map {
        it.toPomodoroSession(json)
    }.firstOrNull()
        ?: PomodoroSessionDomain()

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        val encoded = json.encodeToString(snapshot.toDataStore())
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.ACTIVE_SESSION_KEY] = encoded
            prefs[PreferenceKeys.HAS_ACTIVE_SESSION] = true
        }
    }

    override suspend fun clearActiveSession() {
        dataStore.edit { prefs ->
            prefs.remove(PreferenceKeys.ACTIVE_SESSION_KEY)
            prefs[PreferenceKeys.HAS_ACTIVE_SESSION] = false
        }
    }

    override suspend fun hasActiveSession(): Boolean = getActiveSession() != PomodoroSessionDomain()
}
