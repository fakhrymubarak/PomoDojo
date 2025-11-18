package com.fakhry.pomodojo.features.focus.data.repository

import com.fakhry.pomodojo.core.database.dao.HistorySessionDao
import com.fakhry.pomodojo.core.database.entities.HistorySessionEntity
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.features.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.features.focus.data.mapper.mapToDomain
import com.fakhry.pomodojo.features.focus.data.mapper.toHistoryEntity
import com.fakhry.pomodojo.features.focus.domain.repository.HistorySessionRepository
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import com.fakhry.pomodojo.shared.domain.states.DomainResult
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.min
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class HistorySessionRepositoryImpl(
    private val historyDao: HistorySessionDao,
    private val dispatcher: DispatcherProvider,
) : HistorySessionRepository {
    private val timeZoneUtc = TimeZone.UTC
    private val userTimeZone = TimeZone.currentSystemDefault()

    override suspend fun insertHistory(session: PomodoroSessionDomain) =
        withContext(dispatcher.io) {
            val entity = session.toHistoryEntity()
            val (startOfDay, endOfDay) = dayBounds(entity.dateStartedEpochMs)
            val todaysEntry = historyDao.getSessionsBetween(startOfDay, endOfDay)
                .mergeEntries()
                ?.mergeWith(entity) ?: entity
            historyDao.insertFinishedSession(todaysEntry)
        }

    override suspend fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain> =
        withContext(dispatcher.io) {
            val (startEpochMs, endEpochMs) = yearBounds(year)
            val focusMinutesThisYear = async {
                historyDao.getTotalFocusMinutesBetween(startEpochMs, endEpochMs)
            }
            val availableYears = async { historyDao.getAvailableYears() }
            val histories = async {
                historyDao.getSessionsBetween(startEpochMs, endEpochMs).mapToDomain(userTimeZone)
            }

            DomainResult.Success(
                PomodoroHistoryDomain(
                    focusMinutesThisYear = focusMinutesThisYear.await(),
                    availableYears = availableYears.await(),
                    histories = histories.await(),
                ),
            )
        }

    @OptIn(ExperimentalTime::class)
    private fun yearBounds(year: Int): Pair<Long, Long> {
        val start =
            LocalDate(year, Month.JANUARY, 1).atStartOfDayIn(timeZoneUtc).toEpochMilliseconds()
        val end =
            LocalDate(year + 1, Month.JANUARY, 1).atStartOfDayIn(timeZoneUtc).toEpochMilliseconds()
        return start to end
    }

    @OptIn(ExperimentalTime::class)
    private fun dayBounds(epochMs: Long): Pair<Long, Long> {
        val sessionDate = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(userTimeZone).date
        val start = sessionDate.atStartOfDayIn(userTimeZone).toEpochMilliseconds()
        val end = sessionDate.plus(DatePeriod(days = 1)).atStartOfDayIn(userTimeZone)
            .toEpochMilliseconds()
        return start to end
    }

    private fun List<HistorySessionEntity>.mergeEntries(): HistorySessionEntity? {
        if (isEmpty()) return null
        val sorted = sortedBy { it.dateStartedEpochMs }
        return sorted.drop(1).fold(sorted.first()) { acc, entity ->
            acc.mergeWith(entity)
        }
    }

    private fun HistorySessionEntity.mergeWith(other: HistorySessionEntity): HistorySessionEntity =
        copy(
            dateStartedEpochMs = min(dateStartedEpochMs, other.dateStartedEpochMs),
            totalFocusMinutes = totalFocusMinutes + other.totalFocusMinutes,
            totalBreakMinutes = totalBreakMinutes + other.totalBreakMinutes,
        )
}
