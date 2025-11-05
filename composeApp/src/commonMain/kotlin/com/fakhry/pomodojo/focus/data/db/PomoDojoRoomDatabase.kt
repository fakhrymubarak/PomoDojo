package com.fakhry.pomodojo.focus.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.FinishedSessionEntity

@Database(
    entities = [ActiveSessionEntity::class, FinishedSessionEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(PomoDojoRoomDatabaseConstructor::class)
abstract class PomoDojoRoomDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object PomoDojoRoomDatabaseConstructor : RoomDatabaseConstructor<PomoDojoRoomDatabase> {
    override fun initialize(): PomoDojoRoomDatabase
}

internal const val POMO_DOJO_DATABASE_NAME = "pomodojo.db"
