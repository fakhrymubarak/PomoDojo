package com.fakhry.pomodojo

import android.content.Context
import com.fakhry.pomodojo.core.database.AndroidFocusDatabaseHolder
import com.fakhry.pomodojo.core.datastore.AndroidPreferencesDataStoreProvider
import com.fakhry.pomodojo.core.framework.audio.AndroidSoundPlayer

internal object AndroidAppDependenciesInitializer {
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
