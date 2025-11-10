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
    suspend fun getSessionsBetween(
        startInclusive: Long,
        endExclusive: Long,
    ): List<HistorySessionEntity>

    @Query(
        """
        SELECT COALESCE(SUM(total_focus_minutes), 0)
        FROM history_sessions
        WHERE date_finished >= :startInclusive
          AND date_finished < :endExclusive
        """,
    )
    suspend fun getTotalFocusMinutesBetween(startInclusive: Long, endExclusive: Long): Int

    @Query(
        """
        SELECT DISTINCT CAST(strftime('%Y', date_finished / 1000, 'unixepoch') AS INTEGER)
        FROM history_sessions
        ORDER BY 1 DESC
        """,
    )
    suspend fun getAvailableYears(): List<Int>
}
