package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.focus.data.db.FocusSessionDao
import com.fakhry.pomodojo.focus.data.db.HistorySessionDao
import com.fakhry.pomodojo.focus.data.db.PomoDojoRoomDatabase
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionHourSplitEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionSegmentEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionWithRelations
import com.fakhry.pomodojo.focus.data.model.entities.HistorySessionEntity
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType

class RoomPomodoroSessionRepository(
    private val focusDao: FocusSessionDao,
    private val historyDao: HistorySessionDao,
) : PomodoroSessionRepository {
    constructor(database: PomoDojoRoomDatabase) : this(
        focusDao = database.focusSessionDao(),
        historyDao = database.historySessionDao(),
    )

    override suspend fun getActiveSession(): PomodoroSessionDomain {
        val snapshot =
            focusDao.getActiveSessionWithRelations()
                ?: throw IllegalStateException("No active session stored in database.")
        return snapshot.toDomain()
    }

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        focusDao.clearActiveSession()
        persistSnapshot(snapshot, sessionIdOverride = null)
    }

    override suspend fun updateActiveSession(snapshot: PomodoroSessionDomain) {
        val existingId = focusDao.getActiveSessionId()
        persistSnapshot(snapshot, sessionIdOverride = existingId)
    }

    override suspend fun completeSession(snapshot: PomodoroSessionDomain) {
        historyDao.insertFinishedSession(snapshot.toHistoryEntity())
        focusDao.clearActiveSession()
    }

    override suspend fun clearActiveSession() {
        focusDao.clearActiveSession()
    }

    override suspend fun hasActiveSession(): Boolean = focusDao.hasActiveSession()

    private suspend fun persistSnapshot(
        snapshot: PomodoroSessionDomain,
        sessionIdOverride: Long?,
    ) {
        val entity = snapshot.toEntity(sessionIdOverride)
        val upsertId = focusDao.upsertActiveSession(entity)
        val resolvedId = if (upsertId == -1L) entity.sessionId else upsertId
        focusDao.replaceSegments(resolvedId, snapshot.toSegmentEntities(resolvedId))
        focusDao.replaceHourSplits(resolvedId, snapshot.toHourSplitEntities(resolvedId))
    }

    private fun ActiveSessionWithRelations.toDomain(): PomodoroSessionDomain =
        PomodoroSessionDomain(
            totalCycle = session.totalCycle,
            startedAtEpochMs = session.startedAtEpochMs,
            elapsedPauseEpochMs = session.elapsedPausedEpochMs,
            timeline =
            TimelineDomain(
                segments =
                segments
                    .sortedBy { it.segmentIndex }
                    .map { it.toDomain() },
                hourSplits =
                hourSplits
                    .sortedBy { it.position }
                    .map { it.minutes },
            ),
            quote =
            QuoteContent(
                id = session.quoteId,
                text = session.quoteText,
                character = session.quoteCharacter,
                sourceTitle = session.quoteSourceTitle,
                metadata = session.quoteMetadata,
            ),
        )

    private fun ActiveSessionSegmentEntity.toDomain(): TimerSegmentsDomain = TimerSegmentsDomain(
        type = type,
        cycleNumber = cycleNumber,
        timer =
        TimerDomain(
            durationEpochMs = durationEpochMs,
            finishedInMillis = finishedInMillis,
            startedPauseTime = startedPauseTime,
            elapsedPauseTime = elapsedPauseTime,
        ),
        timerStatus = timerStatus,
    )

    private fun PomodoroSessionDomain.toEntity(sessionIdOverride: Long?): ActiveSessionEntity =
        ActiveSessionEntity(
            sessionId = sessionIdOverride ?: 0L,
            totalCycle = totalCycle,
            startedAtEpochMs = startedAtEpochMs,
            elapsedPausedEpochMs = elapsedPauseEpochMs,
            quoteId = quote.id,
            quoteText = quote.text,
            quoteCharacter = quote.character,
            quoteSourceTitle = quote.sourceTitle,
            quoteMetadata = quote.metadata,
        )

    private fun PomodoroSessionDomain.toSegmentEntities(
        sessionId: Long,
    ): List<ActiveSessionSegmentEntity> = timeline.segments.mapIndexed { index, segment ->
        ActiveSessionSegmentEntity(
            sessionId = sessionId,
            segmentIndex = index,
            type = segment.type,
            cycleNumber = segment.cycleNumber,
            durationEpochMs = segment.timer.durationEpochMs,
            finishedInMillis = segment.timer.finishedInMillis,
            startedPauseTime = segment.timer.startedPauseTime,
            elapsedPauseTime = segment.timer.elapsedPauseTime,
            timerStatus = segment.timerStatus,
        )
    }

    private fun PomodoroSessionDomain.toHourSplitEntities(
        sessionId: Long,
    ): List<ActiveSessionHourSplitEntity> = timeline.hourSplits.mapIndexed { index, minutes ->
        ActiveSessionHourSplitEntity(
            sessionId = sessionId,
            position = index,
            minutes = minutes,
        )
    }

    private fun PomodoroSessionDomain.toHistoryEntity(): HistorySessionEntity {
        val totalFocusMinutes =
            timeline.segments
                .filter { it.type == TimerType.FOCUS }
                .sumOf { it.timer.durationEpochMs }
                .toMinutes()
        val totalBreakMinutes =
            timeline.segments
                .filter { it.type != TimerType.FOCUS }
                .sumOf { it.timer.durationEpochMs }
                .toMinutes()
        val totalDuration = timeline.segments.sumOf { it.timer.durationEpochMs }
        val finishedAt = startedAtEpochMs + totalDuration
        return HistorySessionEntity(
            id = startedAtEpochMs,
            dateStartedEpochMs = startedAtEpochMs,
            dateFinishedEpochMs = finishedAt,
            totalFocusMinutes = totalFocusMinutes,
            totalBreakMinutes = totalBreakMinutes,
        )
    }

    private fun Long.toMinutes(): Int = (this / MILLIS_PER_MINUTE).toInt()

    private companion object {
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
