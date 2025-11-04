package com.fakhry.pomodojo.focus.data.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finished_sessions")
data class FinishedSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "started_at_epoch_ms")
    val startedAtEpochMs: Long,
    @ColumnInfo(name = "completed_at_epoch_ms")
    val completedAtEpochMs: Long,
    @ColumnInfo(name = "completed_local_date")
    val completedLocalDate: String,
    @ColumnInfo(name = "year")
    val year: Int,
    @ColumnInfo(name = "focus_minutes")
    val focusMinutes: Int,
    @ColumnInfo(name = "break_minutes")
    val breakMinutes: Int,
    @ColumnInfo(name = "cycle_count")
    val cycleCount: Int,
    @ColumnInfo(name = "quote_id")
    val quoteId: String?,
    @ColumnInfo(name = "quote_text")
    val quoteText: String?,
)