package com.fakhry.pomodojo.focus.data.db

import android.content.Context
import androidx.room.Room

actual fun createDatabase(): PomoDojoRoomDatabase = AndroidFocusDatabaseHolder.database

fun initAndroidFocusDatabase(context: Context) {
    AndroidFocusDatabaseHolder.initialize(context.applicationContext)
}

internal object AndroidFocusDatabaseHolder {
    private const val DATABASE_NAME = "pomodojo.db"

    private lateinit var appContext: Context

    val database: PomoDojoRoomDatabase by lazy {
        check(::appContext.isInitialized) {
            "Android focus database not initialized. Call initAndroidFocusDatabase() first."
        }
        Room.databaseBuilder(
            appContext,
            PomoDojoRoomDatabase::class.java,
            POMO_DOJO_DATABASE_NAME,
        ).addMigrations(*FocusMigrations).build()
    }

    fun initialize(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context
        }
    }

    fun requireContext(): Context {
        check(::appContext.isInitialized) {
            "Android focus database not initialized. Call initAndroidFocusDatabase() first."
        }
        return appContext
    }
}
