package com.fakhry.pomodojo

import android.content.Context
import com.fakhry.pomodojo.core.database.AndroidFocusDatabaseHolder
import com.fakhry.pomodojo.focus.domain.usecase.AndroidSegmentCompletionSoundPlayer
import com.fakhry.pomodojo.preferences.data.source.AndroidPreferencesDataStoreProvider

internal object AndroidAppDependenciesInitializer {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context
        }
        AndroidPreferencesDataStoreProvider.initialize(context)
        AndroidFocusDatabaseHolder.initialize(context)
        AndroidSegmentCompletionSoundPlayer.initialize(context)
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
        AndroidSegmentCompletionSoundPlayer.destroy()
        appContext = null
    }
}
