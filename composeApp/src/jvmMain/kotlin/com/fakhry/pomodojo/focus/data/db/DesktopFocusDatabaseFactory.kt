package com.fakhry.pomodojo.focus.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.nio.file.Files
import java.nio.file.Path

actual fun createDatabase(): PomoDojoRoomDatabase = DesktopFocusDatabaseHolder.database

private object DesktopFocusDatabaseHolder {
    private val databasePath: Path by lazy {
        val home = System.getProperty("user.home")
        val directory = Path.of(home, ".pomodojo")
        Files.createDirectories(directory)
        directory.resolve("pomodojo.db")
    }

    val database: PomoDojoRoomDatabase by lazy {
        Room.databaseBuilder<PomoDojoRoomDatabase>(
            name = databasePath.toString(),
        ).setDriver(BundledSQLiteDriver()).build()
    }
}
