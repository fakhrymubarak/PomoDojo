package com.fakhry.pomodojo.focus.data.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_sessions")
data class ActiveSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "focus_minutes")
    val focusMinutes: Int,
    @ColumnInfo(name = "break_minutes")
    val breakMinutes: Int,
    @ColumnInfo(name = "long_break_minutes")
    val longBreakMinutes: Int,
    @ColumnInfo(name = "auto_start_next_phase")
    val autoStartNextPhase: Boolean,
    @ColumnInfo(name = "auto_start_breaks")
    val autoStartBreaks: Boolean,
    @ColumnInfo(name = "phase_remaining_seconds")
    val phaseRemainingSeconds: Int,
    @ColumnInfo(name = "phase_total_seconds")
    val currentPhaseTotalSeconds: Int,
    @ColumnInfo(name = "completed_cycles")
    val completedCycles: Int,
    @ColumnInfo(name = "total_cycles")
    val totalCycles: Int,
    @ColumnInfo(name = "current_phase")
    val currentPhase: String,
    @ColumnInfo(name = "phase_started_at_epoch_ms")
    val phaseStartedAtEpochMs: Long,
    @ColumnInfo(name = "quote_id")
    val quoteId: String?,
    @ColumnInfo(name = "quote_text")
    val quoteText: String?,
    @ColumnInfo(name = "quote_character")
    val quoteCharacter: String?,
    @ColumnInfo(name = "quote_source")
    val quoteSource: String?,
    @ColumnInfo(name = "quote_metadata")
    val quoteMetadata: String?,
    @ColumnInfo(name = "started_at_epoch_ms")
    val startedAtEpochMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long,
)