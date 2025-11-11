package com.fakhry.pomodojo.focus.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionHourSplitEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionSegmentEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionWithRelations

@Dao
interface FocusSessionDao {
    @Query("SELECT EXISTS(SELECT 1 FROM active_sessions LIMIT 1)")
    suspend fun hasActiveSession(): Boolean

    @Transaction
    @Query("SELECT * FROM active_sessions LIMIT 1")
    suspend fun getActiveSessionWithRelations(): ActiveSessionWithRelations?

    @Query("SELECT session_id FROM active_sessions LIMIT 1")
    suspend fun getActiveSessionId(): Long?

    @Upsert
    suspend fun upsertActiveSession(entity: ActiveSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<ActiveSessionSegmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourSplits(entities: List<ActiveSessionHourSplitEntity>)

    @Query("DELETE FROM active_session_segments WHERE session_id = :sessionId")
    suspend fun deleteSegments(sessionId: Long)

    @Query("DELETE FROM active_session_hour_splits WHERE session_id = :sessionId")
    suspend fun deleteHourSplits(sessionId: Long)

    @Query("DELETE FROM active_sessions")
    suspend fun clearActiveSession()

    @Transaction
    suspend fun replaceSegments(sessionId: Long, segments: List<ActiveSessionSegmentEntity>) {
        deleteSegments(sessionId)
        if (segments.isNotEmpty()) {
            insertSegments(segments)
        }
    }

    @Transaction
    suspend fun replaceHourSplits(sessionId: Long, splits: List<ActiveSessionHourSplitEntity>) {
        deleteHourSplits(sessionId)
        if (splits.isNotEmpty()) {
            insertHourSplits(splits)
        }
    }
}
