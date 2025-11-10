package com.fakhry.pomodojo.dashboard.data.repository

import com.fakhry.pomodojo.dashboard.domain.model.HistoryDomain
import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.dashboard.domain.repository.PomodoroHistoryRepository
import com.fakhry.pomodojo.focus.data.db.HistorySessionDao
import com.fakhry.pomodojo.focus.data.model.entities.HistorySessionEntity
import com.fakhry.pomodojo.ui.state.DomainResult
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class PomodoroHistoryRepositoryImpl(
    private val historyDao: HistorySessionDao,
    private val timeZone: TimeZone = TimeZone.UTC,
) : PomodoroHistoryRepository {

    override fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain> = runBlocking {
        val (startEpochMs, endEpochMs) = yearBounds(year)
        val focusMinutesThisYear = historyDao.getTotalFocusMinutesBetween(startEpochMs, endEpochMs)
        val availableYears = historyDao.getAvailableYears()
        val histories =
            historyDao
                .getSessionsBetween(startEpochMs, endEpochMs)
                .mapToDomain(timeZone)

        DomainResult.Success(
            PomodoroHistoryDomain(
                focusMinutesThisYear = focusMinutesThisYear,
                availableYears = availableYears,
                histories = histories,
            ),
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun yearBounds(year: Int): Pair<Long, Long> {
        val start =
            LocalDate(year, Month.JANUARY, 1)
                .atStartOfDayIn(timeZone)
                .toEpochMilliseconds()
        val end =
            LocalDate(year + 1, Month.JANUARY, 1)
                .atStartOfDayIn(timeZone)
                .toEpochMilliseconds()
        return start to end
    }
}

private fun List<HistorySessionEntity>.mapToDomain(timeZone: TimeZone): List<HistoryDomain> =
    map { it.toDomain(timeZone) }

@OptIn(ExperimentalTime::class)
private fun HistorySessionEntity.toDomain(timeZone: TimeZone): HistoryDomain {
    val date =
        Instant.fromEpochMilliseconds(dateFinishedEpochMs)
            .toLocalDateTime(timeZone)
            .date
            .toString()
    return HistoryDomain(
        date = date,
        focusMinutes = totalFocusMinutes,
        breakMinutes = totalBreakMinutes,
    )
}
