package com.fakhry.pomodojo.focus.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.FinishedSessionEntity

@Database(
    entities = [ActiveSessionEntity::class, FinishedSessionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PomoDojoRoomDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
}

