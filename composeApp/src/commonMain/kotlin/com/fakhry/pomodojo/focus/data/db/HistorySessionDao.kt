package com.fakhry.pomodojo.focus.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fakhry.pomodojo.focus.data.model.entities.HistorySessionEntity

@Dao
interface HistorySessionDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertFinishedSession(entity: HistorySessionEntity)

    @Query(
        """
                SELECT * FROM history_sessions
        WHERE date_finished >= :startInclusive
          AND date_finished < :endExclusive
        ORDER BY date_finished DESC
    """,
    )
    suspend fun getByYear(
        startInclusive: Long,
        endExclusive: Long,
    ): List<HistorySessionEntity>
}
