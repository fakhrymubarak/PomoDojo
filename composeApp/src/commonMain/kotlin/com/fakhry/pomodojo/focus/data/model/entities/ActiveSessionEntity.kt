package com.fakhry.pomodojo.focus.data.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the state of an active Pomodoro session persisted in the database.
 *
 * This entity captures the current progress of a session, including its timing,
 * status, and the configuration settings it was started with. It also links
 * to a quote displayed during the session.
 *
 * @property sessionId The unique identifier for the session.
 * @property startedAtEpochMs The timestamp (in milliseconds since epoch) when the session was started.
 * @property elapsedPausedEpochMs The total accumulated time (in milliseconds) the session has spent in a paused state.
 * @property sessionStatus The current status of the session (e.g., "RUNNING", "PAUSED").
 * @property repeatCount The total number of focus/break cycles to complete in this session.
 * @property focusMinutes The duration of a single focus period in minutes.
 * @property breakMinutes The duration of a short break period in minutes.
 * @property longBreakEnabled A flag (1 for true, 0 for false) indicating if long breaks are enabled.
 * @property longBreakAfter The number of focus sessions to complete before a long break.
 * @property longBreakMinutes The duration of a long break period in minutes.
 * @property quoteId The identifier for the quote associated with this session.
 */
@Entity(tableName = "active_sessions")
data class ActiveSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    val sessionId: Long = 0L,
    @ColumnInfo(name = "started_at_epoch_ms")
    val startedAtEpochMs: Long,
    @ColumnInfo(name = "elapsed_paused_epoch_ms")
    val elapsedPausedEpochMs: Long,
    @ColumnInfo(name = "pause_started_at_epoch_ms")
    val pauseStartedAtEpochMs: Long?,
    @ColumnInfo(name = "sessionStatus")
    val sessionStatus: String,
    @ColumnInfo(name = "repeat_count")
    val repeatCount: Int,
    @ColumnInfo(name = "focus_minutes")
    val focusMinutes: Int,
    @ColumnInfo(name = "break_minutes")
    val breakMinutes: Int,
    @ColumnInfo(name = "long_break_enabled")
    val longBreakEnabled: Boolean,
    @ColumnInfo(name = "long_break_after")
    val longBreakAfter: Int,
    @ColumnInfo(name = "long_break_minutes")
    val longBreakMinutes: Int,
    @ColumnInfo(name = "quote_id")
    val quoteId: String,
)
