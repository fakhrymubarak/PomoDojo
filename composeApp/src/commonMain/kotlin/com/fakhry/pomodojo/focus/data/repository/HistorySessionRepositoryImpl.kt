package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.focus.data.db.HistorySessionDao
import com.fakhry.pomodojo.focus.data.mapper.mapToDomain
import com.fakhry.pomodojo.focus.domain.repository.HistorySessionRepository
import com.fakhry.pomodojo.ui.state.DomainResult
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.ExperimentalTime

class HistorySessionRepositoryImpl(
    private val historyDao: HistorySessionDao,
) : HistorySessionRepository {
    private val timeZone: TimeZone = TimeZone.UTC

    override fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain> = runBlocking {
        val (startEpochMs, endEpochMs) = yearBounds(year)
        val focusMinutesThisYear = historyDao.getTotalFocusMinutesBetween(startEpochMs, endEpochMs)
        val availableYears = historyDao.getAvailableYears()
        val histories =
            historyDao.getSessionsBetween(startEpochMs, endEpochMs).mapToDomain(timeZone)

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
        val start = LocalDate(year, Month.JANUARY, 1).atStartOfDayIn(timeZone).toEpochMilliseconds()
        val end =
            LocalDate(year + 1, Month.JANUARY, 1).atStartOfDayIn(timeZone).toEpochMilliseconds()
        return start to end
    }
}
