package com.fakhry.pomodojo.core.database

import android.content.Context
import androidx.room.Room

internal actual val POMO_DOJO_DATABASE_NAME: String = BuildConfig.LOCAL_DB_NAME

actual fun createDatabase(): PomoDojoRoomDatabase = AndroidFocusDatabaseHolder.database

object AndroidFocusDatabaseHolder {
    private var appContext: Context? = null

    val database: PomoDojoRoomDatabase by lazy {
        check(appContext != null) {
            "Android focus database not initialized. Call initAndroidFocusDatabase() first."
        }
        Room.databaseBuilder(
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
