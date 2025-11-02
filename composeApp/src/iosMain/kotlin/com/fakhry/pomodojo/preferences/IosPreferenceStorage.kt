package com.fakhry.pomodojo.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual fun provideDataStore(): DataStore<Preferences> {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val directory = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).firstOrNull() as? String ?: error("Unable to locate Documents directory for DataStore.")

    val path: Path = directory
        .plus("/")
        .plus(PREFERENCES_FILE_NAME)
        .toPath()

    return PreferenceDataStoreFactory.createWithPath(
        scope = scope,
        produceFile = { path },
    )
}
