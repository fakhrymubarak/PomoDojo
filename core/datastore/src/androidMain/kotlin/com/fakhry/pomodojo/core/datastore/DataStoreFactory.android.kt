package com.fakhry.pomodojo.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import org.koin.core.context.GlobalContext

internal actual fun createDatastore(): DataStore<Preferences> {
    val koin = GlobalContext.get()
    val dataStoreProvider: AndroidDataStoreProvider = koin.get()
    return dataStoreProvider.dataStore
}

class AndroidDataStoreProvider(appContext: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = {
                appContext.filesDir
                    .resolve(PREFERENCES_FILE_NAME)
                    .absolutePath
                    .toPath()
            },
        )
    }
}
