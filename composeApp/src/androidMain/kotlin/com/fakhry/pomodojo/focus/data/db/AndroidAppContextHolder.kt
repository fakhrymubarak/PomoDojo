package com.fakhry.pomodojo.focus.data.db

import android.content.Context

fun initAndroidAppContextHolder(context: Context) {
    AndroidAppContextHolder.initialize(context)
}

internal object AndroidAppContextHolder {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context
        }
    }

    fun requireContext(): Context {
        check(::appContext.isInitialized) {
            "Android context holder not initialized. Call initAndroidAppContextHolder() first."
        }
        return appContext
    }
}
