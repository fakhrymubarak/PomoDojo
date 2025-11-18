package com.fakhry.pomodojo.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.nio.file.Files
import java.nio.file.Path

internal actual val POMO_DOJO_DATABASE_NAME: String = "pomodojo.db"

actual fun createDatabase(): PomoDojoRoomDatabase = DesktopFocusDatabaseHolder.database

private object DesktopFocusDatabaseHolder {
    private val databasePath: Path by lazy {
        val home = System.getProperty("user.home")
        val directory = Path.of(home, ".pomodojo")
        Files.createDirectories(directory)
        directory.resolve(POMO_DOJO_DATABASE_NAME)
    }

    val database: PomoDojoRoomDatabase by lazy {
        Room.databaseBuilder<PomoDojoRoomDatabase>(databasePath.toString())
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
