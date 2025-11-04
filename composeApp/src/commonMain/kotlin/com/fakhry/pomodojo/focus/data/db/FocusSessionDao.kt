package com.fakhry.pomodojo.focus.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.FinishedSessionEntity

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM active_sessions LIMIT 1")
    suspend fun getActiveSession(): ActiveSessionEntity?

    @Upsert
    suspend fun upsertActiveSession(entity: ActiveSessionEntity)

    @Query("DELETE FROM active_sessions")
    suspend fun clearActiveSession()

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertFinishedSession(entity: FinishedSessionEntity)

    @Transaction
    suspend fun completeSession(
        active: ActiveSessionEntity,
        finished: FinishedSessionEntity,
    ) {
        upsertActiveSession(active)
        insertFinishedSession(finished)
        clearActiveSession()
    }
}