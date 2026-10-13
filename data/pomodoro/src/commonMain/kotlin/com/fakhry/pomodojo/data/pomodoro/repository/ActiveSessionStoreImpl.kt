package com.fakhry.pomodojo.data.pomodoro.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.fakhry.pomodojo.core.datastore.PreferenceKeys
import com.fakhry.pomodojo.data.pomodoro.mapper.toDataStore
import com.fakhry.pomodojo.data.pomodoro.mapper.toPomodoroSession
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/** Stores the Session as JSON in DataStore, whose `edit` makes each write atomic. */
class ActiveSessionStoreImpl(
    private val dataStore: DataStore<Preferences>,
) : ActiveSessionStore {
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val session: Flow<PomodoroSessionDomain?> =
        dataStore.data.map { it.toPomodoroSession(json) }

    override suspend fun update(
        transform: (PomodoroSessionDomain?) -> PomodoroSessionDomain?,
    ): PomodoroSessionDomain? {
        var updated: PomodoroSessionDomain? = null
        dataStore.edit { prefs ->
            updated = transform(prefs.toPomodoroSession(json))
            when (val session = updated) {
                null -> prefs.remove(PreferenceKeys.ACTIVE_SESSION_KEY)
                else -> prefs[PreferenceKeys.ACTIVE_SESSION_KEY] =
                    json.encodeToString(session.toDataStore())
            }
        }
        return updated
    }

    override suspend fun take(): PomodoroSessionDomain? {
        var taken: PomodoroSessionDomain? = null
        dataStore.edit { prefs ->
            taken = prefs.toPomodoroSession(json)
            prefs.remove(PreferenceKeys.ACTIVE_SESSION_KEY)
        }
        return taken
    }
}
