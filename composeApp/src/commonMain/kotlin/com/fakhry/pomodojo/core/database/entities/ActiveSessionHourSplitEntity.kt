package com.fakhry.pomodojo.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "active_session_hour_splits",
    primaryKeys = ["session_id", "position"],
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
data class ActiveSessionHourSplitEntity(
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "minutes")
    val minutes: Int,
)
