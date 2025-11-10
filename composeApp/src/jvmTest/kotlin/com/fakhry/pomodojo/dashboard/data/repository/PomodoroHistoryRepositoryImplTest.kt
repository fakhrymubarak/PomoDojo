package com.fakhry.pomodojo.dashboard.data.repository

import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.focus.data.db.HistorySessionDao
import com.fakhry.pomodojo.focus.data.model.entities.HistorySessionEntity
import com.fakhry.pomodojo.ui.state.DomainResult
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PomodoroHistoryRepositoryImplTest {
    private val fakeDao = FakeHistorySessionDao()
    private val repository = PomodoroHistoryRepositoryImpl(fakeDao)

    @Test
    fun `maps dao output into domain model`() {
        fakeDao.totalMinutes = 185
        fakeDao.availableYears = listOf(2024, 2023)
        fakeDao.sessions = listOf(
            historyEntity(
                year = 2024,
                month = Month.FEBRUARY,
                day = 10,
                focusMinutes = 25,
                breakMinutes = 5,
            ),
            historyEntity(
                year = 2023,
                month = Month.MARCH,
                day = 11,
                focusMinutes = 50,
                breakMinutes = 10,
            ),
        )

        val result = repository.getHistory(2024) as DomainResult.Success
        val domain: PomodoroHistoryDomain = result.data

        assertEquals(185, domain.focusMinutesThisYear)
        assertEquals(listOf(2024, 2023), domain.availableYears)
        assertEquals(2, domain.histories.size)
        val history = domain.histories.first()
        assertEquals("2024-02-10", history.date)
        assertEquals(25, history.focusMinutes)
        assertEquals(5, history.breakMinutes)
    }

    @Test
    fun `queries dao with year start and end bounds`() {
        repository.getHistory(2023)

        val expectedStart =
            LocalDate(2023, Month.JANUARY, 1).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        val expectedEnd =
            LocalDate(2024, Month.JANUARY, 1).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

        val totalRange = fakeDao.totalMinutesRange
        val sessionsRange = fakeDao.sessionsRange

        assertEquals(expectedStart, totalRange?.first)
        assertEquals(expectedEnd, totalRange?.second)
        assertEquals(expectedStart, sessionsRange?.first)
        assertEquals(expectedEnd, sessionsRange?.second)
    }

    private fun historyEntity(
        year: Int,
        month: Month,
        day: Int,
        focusMinutes: Int,
        breakMinutes: Int,
    ): HistorySessionEntity {
        val epoch = LocalDate(year, month, day).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        return HistorySessionEntity(
            id = 1L,
            dateStartedEpochMs = epoch,
            dateFinishedEpochMs = epoch,
            totalFocusMinutes = focusMinutes,
            totalBreakMinutes = breakMinutes,
        )
    }

    private class FakeHistorySessionDao : HistorySessionDao {
        var totalMinutes = 0
        var totalMinutesRange: Pair<Long, Long>? = null
        var sessionsRange: Pair<Long, Long>? = null
        var availableYears: List<Int> = emptyList()
        var sessions: List<HistorySessionEntity> = emptyList()

        override suspend fun insertFinishedSession(entity: HistorySessionEntity) {
            // no-op for tests
        }

        override suspend fun getSessionsBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): List<HistorySessionEntity> {
            sessionsRange = startInclusive to endExclusive
            return sessions
        }

        override suspend fun getTotalFocusMinutesBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): Int {
            totalMinutesRange = startInclusive to endExclusive
            return totalMinutes
        }

        override suspend fun getAvailableYears(): List<Int> = availableYears
    }
}
