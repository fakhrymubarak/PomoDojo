package com.fakhry.pomodojo.focus.data.db

import android.content.Context
import com.fakhry.pomodojo.preferences.data.source.initAndroidPreferenceStorage

internal object AndroidAppInitializer {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context
        }
        initAndroidPreferenceStorage(context)
        initAndroidFocusDatabase(context)
    }

    fun requireContext(): Context {
        check(::appContext.isInitialized) {
            "Android context holder not initialized. Call initAndroidAppContextHolder() first."
        }
        return appContext
    }
}
