package com.fakhry.pomodojo.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_sessions")
data class HistorySessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "date_started")
    val dateStartedEpochMs: Long = 0L,
    @ColumnInfo(name = "total_focus_minutes")
    val totalFocusMinutes: Int,
    @ColumnInfo(name = "total_break_minutes")
    val totalBreakMinutes: Int,
)
