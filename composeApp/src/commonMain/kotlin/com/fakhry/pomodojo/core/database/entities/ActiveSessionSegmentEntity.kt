package com.fakhry.pomodojo.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType

@Entity(
    tableName = "active_session_segments",
    primaryKeys = ["session_id", "segment_index"],
    indices = [
        Index(value = ["session_id"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = ActiveSessionEntity::class,
            parentColumns = ["session_id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ActiveSessionSegmentEntity(
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    @ColumnInfo(name = "segment_index")
    val segmentIndex: Int,
    @ColumnInfo(name = "type")
    val type: TimerType,
    @ColumnInfo(name = "cycle_number")
    val cycleNumber: Int,
    @ColumnInfo(name = "duration_epoch_ms")
    val durationEpochMs: Long,
    @ColumnInfo(name = "finished_in_millis")
    val finishedInMillis: Long,
    @ColumnInfo(name = "started_pause_time")
    val startedPauseTime: Long,
    @ColumnInfo(name = "elapsed_pause_time")
    val elapsedPauseTime: Long,
    @ColumnInfo(name = "timer_status")
    val timerStatus: TimerStatusDomain,
)
