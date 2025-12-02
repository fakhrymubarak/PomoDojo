package com.fakhry.pomodojo.core.database

import android.content.Context
import androidx.room.Room
import org.koin.core.context.GlobalContext

internal actual val POMO_DOJO_DATABASE_NAME: String = BuildConfig.LOCAL_DB_NAME

actual fun createDatabase(): PomoDojoRoomDatabase {
    val koin = GlobalContext.get()
    val databaseHolder: AndroidFocusDatabaseHolder = koin.get()
    return databaseHolder.database
}

class AndroidFocusDatabaseHolder(appContext: Context) {
    val database: PomoDojoRoomDatabase by lazy {
        Room.databaseBuilder(
            appContext!!,
            PomoDojoRoomDatabase::class.java,
            POMO_DOJO_DATABASE_NAME,
        ).build()
    }

}
