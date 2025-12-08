package com.fakhry.pomodojo.data.preferences.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.fakhry.pomodojo.core.datastore.PreferenceKeys
import com.fakhry.pomodojo.domain.preferences.model.AppTheme
import com.fakhry.pomodojo.domain.preferences.model.InitAppPreferences
import com.fakhry.pomodojo.domain.preferences.repository.InitPreferencesRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class InitPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : InitPreferencesRepository {
    override val initPreferences = dataStore.data.map {
        it.toInitPreferences()
    }.distinctUntilChanged()

    override suspend fun updateAppTheme(theme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.APP_THEME] = theme.storageValue
        }
    }

    override suspend fun updateHasActiveSession(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.HAS_ACTIVE_SESSION] = value
        }
    }

    private fun Preferences.toInitPreferences(): InitAppPreferences = InitAppPreferences(
        appTheme = this[PreferenceKeys.APP_THEME] ?: "dark",
        hasActiveSession = this[PreferenceKeys.HAS_ACTIVE_SESSION] ?: false,
    )
}
