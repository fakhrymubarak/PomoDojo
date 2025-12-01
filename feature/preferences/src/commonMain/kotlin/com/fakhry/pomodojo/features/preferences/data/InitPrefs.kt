package com.fakhry.pomodojo.features.preferences.data

import com.fakhry.pomodojo.core.datastore.PreferenceKeys
import com.fakhry.pomodojo.core.datastore.provideDataStore
import com.fakhry.pomodojo.features.preferences.domain.model.InitAppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * This code should run once on MainThread to load the AppTheme and StartDestination.
 * */
fun getInitPreferencesOnMainThread(): InitAppPreferences {
    val preferences = runBlocking {
        runCatching {
            provideDataStore().data.map { prefs ->
                // Take only initial data for more optimal processing
                InitAppPreferences(
                    appTheme = prefs[PreferenceKeys.APP_THEME] ?: "dark",
                    hasActiveSession = prefs[PreferenceKeys.HAS_ACTIVE_SESSION] ?: false,
                )
            }.first()
        }.getOrDefault(InitAppPreferences())
    }
    return preferences
}
