package com.fakhry.pomodojo.focus.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity

@Dao
interface FocusSessionDao {
    @Query("SELECT EXISTS(SELECT 1 FROM active_sessions LIMIT 1)")
    suspend fun hasActiveSession(): Boolean

    @Query("SELECT * FROM active_sessions LIMIT 1")
    suspend fun getActiveSession(): ActiveSessionEntity?

    @Upsert
    suspend fun upsertActiveSession(entity: ActiveSessionEntity)

    @Query("DELETE FROM active_sessions")
    suspend fun clearActiveSession()
}
