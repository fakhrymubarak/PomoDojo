package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.focus.data.db.PomoDojoRoomDatabase
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.HistorySessionEntity
import com.fakhry.pomodojo.focus.domain.model.ActiveFocusSessionDomain
import com.fakhry.pomodojo.focus.domain.model.FocusTimerStatus
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class RoomPomodoroSessionRepository(
    database: PomoDojoRoomDatabase,
) : PomodoroSessionRepository {
    private val activeDao = database.focusSessionDao()
    private val historyDao = database.historySessionDao()

    override suspend fun hasActiveSession(): Boolean = activeDao.hasActiveSession()

    override suspend fun getActiveSession() = activeDao.getActiveSession().toDomain()

    override suspend fun saveActiveSession(snapshot: ActiveFocusSessionDomain) {
        activeDao.upsertActiveSession(snapshot.toEntity())
    }

    override suspend fun updateActiveSession(snapshot: ActiveFocusSessionDomain) {
        activeDao.upsertActiveSession(snapshot.toEntity())
    }

    override suspend fun completeSession(snapshot: ActiveFocusSessionDomain) {
        @OptIn(ExperimentalTime::class)
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val finished = snapshot.toFinishedEntity(currentTime)
        activeDao.clearActiveSession()
        historyDao.insertFinishedSession(finished)
    }

    override suspend fun clearActiveSession() {
        activeDao.clearActiveSession()
    }

    private fun ActiveFocusSessionDomain.toEntity(): ActiveSessionEntity =
        ActiveSessionEntity(
            startedAtEpochMs = startedAtEpochMs,
            elapsedPausedEpochMs = elapsedPauseEpochMs,
            pauseStartedAtEpochMs = pauseStartedAtEpochMs,
            sessionStatus = sessionStatus.name,
            repeatCount = repeatCount,
            focusMinutes = focusMinutes,
            breakMinutes = breakMinutes,
            longBreakEnabled = longBreakEnabled,
            longBreakMinutes = longBreakMinutes,
            longBreakAfter = longBreakAfter,
            quoteId = quoteId,
        )

    private fun ActiveFocusSessionDomain.toFinishedEntity(finishedTime: Long): HistorySessionEntity {
        var totalFocusMinutes = 0
        var totalBreakMinutes = 0
        timelines.forEach {
            when (it.type) {
                TimerType.FOCUS -> totalFocusMinutes += (it.timer.durationEpochMs / 60_000L).toInt()
                else -> totalBreakMinutes += (it.timer.durationEpochMs / 60_000L).toInt()
            }
        }

        return HistorySessionEntity(
            id = sessionId,
            dateStartedEpochMs = startedAtEpochMs,
            dateFinishedEpochMs = finishedTime,
            totalFocusMinutes = totalFocusMinutes,
            totalBreakMinutes = totalBreakMinutes,
        )
    }

    private fun ActiveSessionEntity?.toDomain() =
        this?.run {
            ActiveFocusSessionDomain(
                sessionId = sessionId,
                startedAtEpochMs = startedAtEpochMs,
                elapsedPauseEpochMs = elapsedPausedEpochMs,
                pauseStartedAtEpochMs = pauseStartedAtEpochMs,
                sessionStatus = sessionStatus.toEnumSessionStatus(),
                repeatCount = repeatCount,
                focusMinutes = focusMinutes,
                breakMinutes = breakMinutes,
                longBreakEnabled = longBreakEnabled,
                longBreakAfter = longBreakAfter,
                longBreakMinutes = longBreakMinutes,
                quoteId = quoteId,
            )
        } ?: ActiveFocusSessionDomain()

    private fun String.toEnumSessionStatus() =
        when (this) {
            FocusTimerStatus.PAUSED.name -> FocusTimerStatus.PAUSED
            else -> FocusTimerStatus.RUNNING
        }
}
