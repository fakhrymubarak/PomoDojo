package com.fakhry.pomodojo.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.fakhry.pomodojo.core.database.dao.HistorySessionDao
import com.fakhry.pomodojo.core.database.entities.HistorySessionEntity

internal expect val POMO_DOJO_DATABASE_NAME: String

expect fun createDatabase(): PomoDojoRoomDatabase

@Database(
    entities = [
        HistorySessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(PomoDojoRoomDatabaseConstructor::class)
abstract class PomoDojoRoomDatabase : RoomDatabase() {
    abstract fun historySessionDao(): HistorySessionDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PomoDojoRoomDatabaseConstructor : RoomDatabaseConstructor<PomoDojoRoomDatabase> {
    override fun initialize(): PomoDojoRoomDatabase
}
