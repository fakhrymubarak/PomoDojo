package com.fakhry.pomodojo.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path
import okio.Path.Companion.toPath
import java.io.File

internal actual fun provideDataStore(): DataStore<Preferences> {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val home = System.getProperty("user.home") ?: "."
    val directory = File(home, ".pomodojo").apply {
        if (!exists()) {
            mkdirs()
        }
    }
    val path: Path = File(directory, PREFERENCES_FILE_NAME).absolutePath.toPath()
    return PreferenceDataStoreFactory.createWithPath(
        scope = scope,
        produceFile = { path },
    )
}
