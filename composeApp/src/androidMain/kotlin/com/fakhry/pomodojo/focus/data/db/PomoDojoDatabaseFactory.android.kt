package com.fakhry.pomodojo.focus.data.db

import android.content.Context
import androidx.room.Room

actual fun createDatabase(): PomoDojoRoomDatabase = AndroidFocusDatabaseHolder.database

internal object AndroidFocusDatabaseHolder {
    private var appContext: Context? = null

    val database: PomoDojoRoomDatabase by lazy {
        check(appContext != null) {
            "Android focus database not initialized. Call initAndroidFocusDatabase() first."
        }
        Room
            .databaseBuilder(
                appContext!!,
                PomoDojoRoomDatabase::class.java,
                POMO_DOJO_DATABASE_NAME,
            ).build()
    }

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context
        }
    }

    fun destroy() {
        database.close()
    }
}
