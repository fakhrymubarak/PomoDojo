package com.fakhry.pomodojo.core.datastore.wiring

import com.fakhry.pomodojo.core.datastore.DataStorePreferenceStorage
import com.fakhry.pomodojo.core.datastore.PreferenceKeys
import com.fakhry.pomodojo.core.datastore.PreferenceStorage
import com.fakhry.pomodojo.core.datastore.provideDataStore
import com.fakhry.pomodojo.domain.preferences.model.AppTheme
import com.fakhry.pomodojo.domain.preferences.model.InitAppPreferences
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
                    appTheme = AppTheme.fromStorage(prefs[PreferenceKeys.APP_THEME]),
                    hasActiveSession = prefs[PreferenceKeys.HAS_ACTIVE_SESSION] ?: false,
                )
            }.first()
        }.getOrDefault(InitAppPreferences())
    }
    return preferences
}

/**
 * This will create singleton of [DataStorePreferenceStorage]. Use it wisely.
 * */
fun prefStorageFactory(): PreferenceStorage = DataStorePreferenceStorage(provideDataStore())
