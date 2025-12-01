package com.fakhry.pomodojo.feature.notification

import android.content.Context
import com.fakhry.pomodojo.core.database.AndroidFocusDatabaseHolder
import com.fakhry.pomodojo.core.datastore.AndroidPreferencesDataStoreProvider
import com.fakhry.pomodojo.feature.notification.audio.AndroidSoundPlayer

object AndroidAppDependenciesInitializer {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context
        }
        AndroidPreferencesDataStoreProvider.initialize(context)
        AndroidFocusDatabaseHolder.initialize(context)
        AndroidSoundPlayer.initialize(context)
    }

    fun requireContext(): Context {
        check(appContext != null) {
            "Android context holder not initialized. Call initAndroidAppContextHolder() first."
        }
        return appContext!!
    }

    fun destroy() {
        AndroidPreferencesDataStoreProvider.destroy()
        AndroidFocusDatabaseHolder.destroy()
        AndroidSoundPlayer.destroy()
        appContext = null
    }
}
