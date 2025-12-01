package com.fakhry.pomodojo.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path
import okio.Path.Companion.toPath
import java.io.File

internal actual fun createDatastore(): DataStore<Preferences> =
    DesktopPreferencesDataStoreProvider.dataStore

private object DesktopPreferencesDataStoreProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val dataStore: DataStore<Preferences> by lazy {
        val home = System.getProperty("user.home") ?: "."
        val directory =
            File(home, ".pomodojo").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
        val path: Path = File(directory, PREFERENCES_FILE_NAME).absolutePath.toPath()
        PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = { path },
        )
    }
}
