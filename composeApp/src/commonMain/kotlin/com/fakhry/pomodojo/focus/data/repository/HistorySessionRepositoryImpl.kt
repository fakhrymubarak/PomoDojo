package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.focus.data.db.PomoDojoRoomDatabase
import com.fakhry.pomodojo.focus.data.mapper.mapToDomain
import com.fakhry.pomodojo.focus.data.mapper.toHistoryEntity
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.repository.HistorySessionRepository
import com.fakhry.pomodojo.ui.state.DomainResult
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.ExperimentalTime

class HistorySessionRepositoryImpl(
    database: PomoDojoRoomDatabase,
    private val dispatcher: DispatcherProvider,
) : HistorySessionRepository {
    private val timeZoneUtc = TimeZone.UTC
    private val historyDao = database.historySessionDao()

    override suspend fun insertHistory(session: PomodoroSessionDomain) {
        val entity = session.toHistoryEntity()
        historyDao.insertFinishedSession(entity)
    }

    override suspend fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain> =
        withContext(dispatcher.io) {
            val userTimeZone: TimeZone = TimeZone.of("UTC+7")

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
}
