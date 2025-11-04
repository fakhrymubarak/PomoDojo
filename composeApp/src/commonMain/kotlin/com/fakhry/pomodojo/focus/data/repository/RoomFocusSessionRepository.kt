package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.focus.data.db.FocusSessionDao
import com.fakhry.pomodojo.focus.data.db.PomoDojoRoomDatabase
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.FinishedSessionEntity
import com.fakhry.pomodojo.focus.domain.model.FocusPhase
import com.fakhry.pomodojo.focus.domain.model.FocusSessionSnapshot
import com.fakhry.pomodojo.focus.domain.model.FocusTimerStatus
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.domain.repository.FocusSessionRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

class RoomFocusSessionRepository(
    database: PomoDojoRoomDatabase,
) : FocusSessionRepository {

    private val dao: FocusSessionDao = database.focusSessionDao()

    override suspend fun getActiveSession(): FocusSessionSnapshot? =
        dao.getActiveSession()?.toSnapshot()

    override suspend fun saveActiveSession(snapshot: FocusSessionSnapshot) {
        dao.upsertActiveSession(snapshot.toEntity())
    }

    override suspend fun updateActiveSession(snapshot: FocusSessionSnapshot) {
        dao.upsertActiveSession(snapshot.toEntity())
    }

    override suspend fun completeSession(snapshot: FocusSessionSnapshot) {
        val entity = snapshot.toEntity()
        val finished = snapshot.toFinishedEntity()
        dao.completeSession(entity, finished)
    }

    override suspend fun clearActiveSession() {
        dao.clearActiveSession()
    }

    private fun FocusSessionSnapshot.toEntity(): ActiveSessionEntity = ActiveSessionEntity(
        sessionId = sessionId,
        status = status.name,
        focusMinutes = focusDurationMinutes,
        breakMinutes = shortBreakMinutes,
        longBreakMinutes = longBreakMinutes,
        autoStartNextPhase = autoStartNextPhase,
        autoStartBreaks = autoStartBreaks,
        phaseRemainingSeconds = phaseRemainingSeconds,
        currentPhaseTotalSeconds = currentPhaseTotalSeconds,
        completedCycles = completedCycles,
        totalCycles = totalCycles,
        currentPhase = phase.name,
        phaseStartedAtEpochMs = phaseStartedAtEpochMs,
        quoteId = quote?.id,
        quoteText = quote?.text,
        quoteCharacter = quote?.character,
        quoteSource = quote?.sourceTitle,
        quoteMetadata = quote?.metadata,
        startedAtEpochMs = startedAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs,
    )

    private fun ActiveSessionEntity.toSnapshot(): FocusSessionSnapshot = FocusSessionSnapshot(
        sessionId = sessionId,
        status = FocusTimerStatus.valueOf(status),
        focusDurationMinutes = focusMinutes,
        shortBreakMinutes = breakMinutes,
        longBreakMinutes = longBreakMinutes,
        autoStartNextPhase = autoStartNextPhase,
        autoStartBreaks = autoStartBreaks,
        phaseRemainingSeconds = phaseRemainingSeconds,
        currentPhaseTotalSeconds = currentPhaseTotalSeconds,
        completedCycles = completedCycles,
        totalCycles = totalCycles,
        phase = FocusPhase.valueOf(currentPhase),
        phaseStartedAtEpochMs = phaseStartedAtEpochMs,
        quote = if (quoteId != null || quoteText != null) {
            QuoteContent(
                id = quoteId ?: sessionId,
                text = quoteText ?: "",
                character = quoteCharacter,
                sourceTitle = quoteSource,
                metadata = quoteMetadata,
            )
        } else {
            null
        },
        startedAtEpochMs = startedAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs,
    )

    @OptIn(ExperimentalTime::class)
    private fun FocusSessionSnapshot.toFinishedEntity(): FinishedSessionEntity {
        val completedTimestamp = updatedAtEpochMs
        val completedInstant = Instant.fromEpochMilliseconds(completedTimestamp)
        val localDateTime = completedInstant.toLocalDateTime(TimeZone.Companion.currentSystemDefault())
        val cycleCount = completedCycles.coerceAtLeast(1)
        val totalFocusMinutes = focusDurationMinutes * cycleCount
        val breakCycles = (cycleCount - 1).coerceAtLeast(0)
        val longBreakBonus = if (longBreakMinutes > 0 && cycleCount >= totalCycles) longBreakMinutes else 0
        val totalBreakMinutes = shortBreakMinutes * breakCycles + longBreakBonus

        return FinishedSessionEntity(
            id = sessionId,
            startedAtEpochMs = startedAtEpochMs,
            completedAtEpochMs = completedTimestamp,
            completedLocalDate = localDateTime.date.toString(),
            year = localDateTime.year,
            focusMinutes = totalFocusMinutes,
            breakMinutes = totalBreakMinutes,
            cycleCount = cycleCount,
            quoteId = quote?.id,
            quoteText = quote?.text,
        )
    }
}