package com.fakhry.pomodojo.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath

internal actual fun provideDataStore(): DataStore<Preferences> =
    AndroidPreferencesDataStoreProvider.dataStore

fun initAndroidPreferenceStorage(context: Context) {
    AndroidPreferencesDataStoreProvider.initialize(context.applicationContext)
}

private object AndroidPreferencesDataStoreProvider {
    private lateinit var appContext: Context

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val dataStore: DataStore<Preferences> by lazy {
        check(::appContext.isInitialized) {
            "Android Preferences storage not initialized. Call initAndroidPreferenceStorage() first."
        }
        PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = {
                appContext.filesDir.resolve(preferencesFileName()).absolutePath.toPath()
            },
        )
    }

    fun initialize(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context
        }
    }
}
