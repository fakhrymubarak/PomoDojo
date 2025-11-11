package com.fakhry.pomodojo.focus.data.model.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ActiveSessionWithRelations(
    @Embedded
    val session: ActiveSessionEntity,
    @Relation(
        parentColumn = "session_id",
        entityColumn = "session_id",
        entity = ActiveSessionSegmentEntity::class,
    )
    val segments: List<ActiveSessionSegmentEntity>,
    @Relation(
        parentColumn = "session_id",
        entityColumn = "session_id",
        entity = ActiveSessionHourSplitEntity::class,
    )
    val hourSplits: List<ActiveSessionHourSplitEntity>,
)
