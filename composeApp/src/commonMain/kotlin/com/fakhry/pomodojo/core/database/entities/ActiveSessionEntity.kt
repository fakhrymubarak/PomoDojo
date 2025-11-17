package com.fakhry.pomodojo.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the persisted snapshot of an active Pomodoro session. Only the immutable, top-level
 * properties live on this table; timeline segments and hour splits are normalized into their own
 * tables so they can be queried and updated independently.
 */
@Entity(tableName = "active_sessions")
data class ActiveSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    val sessionId: Long = 0L,
    @ColumnInfo(name = "total_cycle")
    val totalCycle: Int,
    @ColumnInfo(name = "started_at_epoch_ms")
    val startedAtEpochMs: Long,
    @ColumnInfo(name = "elapsed_paused_epoch_ms")
    val elapsedPausedEpochMs: Long,
    @ColumnInfo(name = "quote_id")
    val quoteId: String,
    @ColumnInfo(name = "quote_text")
    val quoteText: String,
    @ColumnInfo(name = "quote_character")
    val quoteCharacter: String?,
    @ColumnInfo(name = "quote_source_title")
    val quoteSourceTitle: String?,
    @ColumnInfo(name = "quote_metadata")
    val quoteMetadata: String?,
)
