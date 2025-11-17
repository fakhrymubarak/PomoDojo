package com.fakhry.pomodojo.focus.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionHourSplitEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionSegmentEntity
import com.fakhry.pomodojo.focus.data.model.entities.HistorySessionEntity

@Database(
    entities = [
        ActiveSessionEntity::class,
        ActiveSessionSegmentEntity::class,
        ActiveSessionHourSplitEntity::class,
        HistorySessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(PomoDojoRoomDatabaseConstructor::class)
abstract class PomoDojoRoomDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao

    abstract fun historySessionDao(): HistorySessionDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "KotlinNoActualForExpect")
expect object PomoDojoRoomDatabaseConstructor : RoomDatabaseConstructor<PomoDojoRoomDatabase> {
    override fun initialize(): PomoDojoRoomDatabase
}

internal expect val POMO_DOJO_DATABASE_NAME: String
