package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.focus.data.db.HistorySessionDao
import com.fakhry.pomodojo.focus.data.model.entities.HistorySessionEntity
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.ui.state.DomainResult
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class HistorySessionRepositoryImplTest {
    private val fakeDao = FakeHistorySessionDao()
    private val testDispatcher = StandardTestDispatcher()
    private val repository = HistorySessionRepositoryImpl(
        fakeDao,
        DispatcherProvider(testDispatcher),
    )

    private val minuteMillis = 60_000L

    @Test
    fun `insert history will update today history item`() = runTest(testDispatcher) {
        val sessionDate =
            LocalDate(2024, Month.JANUARY, 10).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        val existing = HistorySessionEntity(
            id = 42L,
            dateStartedEpochMs = sessionDate,
            totalFocusMinutes = 25,
            totalBreakMinutes = 5,
        )
        fakeDao.sessions = listOf(existing)

        val newSessionStart = sessionDate + 5 * minuteMillis
        val seed = Random.nextInt(1, 10)
        val newSessionFocus = seed * 10
        val newSessionBreak = seed * 5
        val session = pomodoroSession(
            startedAtMs = newSessionStart,
            focusMinutes = newSessionFocus,
            breakMinutes = newSessionBreak,
        )

        repository.insertHistory(session)

        val inserted = fakeDao.lastInserted ?: error("Expected history insert")
        assertEquals(existing.id, inserted.id)
        assertEquals(existing.dateStartedEpochMs, inserted.dateStartedEpochMs)
        assertEquals(existing.totalFocusMinutes + newSessionFocus, inserted.totalFocusMinutes)
        assertEquals(existing.totalBreakMinutes + newSessionBreak, inserted.totalBreakMinutes)
    }

    @Test
    fun `maps dao output into domain model`() = runTest(testDispatcher) {
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
    fun `queries dao with year start and end bounds`() = runTest(testDispatcher) {
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
            totalFocusMinutes = focusMinutes,
            totalBreakMinutes = breakMinutes,
        )
    }

    private fun pomodoroSession(
        startedAtMs: Long,
        focusMinutes: Int,
        breakMinutes: Int,
    ): PomodoroSessionDomain {
        val segments = buildList {
            if (focusMinutes > 0) {
                add(timerSegment(TimerType.FOCUS, focusMinutes))
            }
            if (breakMinutes > 0) {
                add(timerSegment(TimerType.SHORT_BREAK, breakMinutes))
            }
        }
        return PomodoroSessionDomain(
            totalCycle = segments.size,
            startedAtEpochMs = startedAtMs,
            elapsedPauseEpochMs = 0L,
            timeline = TimelineDomain(
                segments = segments,
                hourSplits = emptyList(),
            ),
        )
    }

    private fun timerSegment(type: TimerType, minutes: Int) = TimerSegmentsDomain(
        type = type,
        cycleNumber = 1,
        timer = TimerDomain(
            progress = 1f,
            durationEpochMs = minutes * minuteMillis,
            finishedInMillis = minutes * minuteMillis,
        ),
        timerStatus = TimerStatusDomain.COMPLETED,
    )

    private class FakeHistorySessionDao : HistorySessionDao {
        var totalMinutes = 0
        var totalMinutesRange: Pair<Long, Long>? = null
        var sessionsRange: Pair<Long, Long>? = null
        var availableYears: List<Int> = emptyList()
        var sessions: List<HistorySessionEntity> = emptyList()
        var lastInserted: HistorySessionEntity? = null

        override suspend fun insertFinishedSession(entity: HistorySessionEntity) {
            lastInserted = entity
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
