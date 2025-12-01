package com.fakhry.pomodojo.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import okio.Path.Companion.toPath

internal actual fun createDatastore(): DataStore<Preferences> =
    AndroidPreferencesDataStoreProvider.dataStore

object AndroidPreferencesDataStoreProvider {
    private var appContext: Context? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val dataStore: DataStore<Preferences> by lazy {
        check(appContext != null) {
            "Android Preferences storage not initialized. Call initAndroidPreferenceStorage() first."
        }
        PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = {
                appContext!!.filesDir
                    .resolve(PREFERENCES_FILE_NAME)
                    .absolutePath
                    .toPath()
            },
        )
    }

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context
        }
    }

    fun destroy() {
        appContext = null
        scope.cancel()
    }
}
