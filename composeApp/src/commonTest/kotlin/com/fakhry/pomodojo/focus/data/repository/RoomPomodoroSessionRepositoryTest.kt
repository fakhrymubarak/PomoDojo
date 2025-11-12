package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.focus.data.db.FocusSessionDao
import com.fakhry.pomodojo.focus.data.db.HistorySessionDao
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionHourSplitEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionSegmentEntity
import com.fakhry.pomodojo.focus.data.model.entities.ActiveSessionWithRelations
import com.fakhry.pomodojo.focus.data.model.entities.HistorySessionEntity
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoomPomodoroSessionRepositoryTest {
    @Test
    fun `saveActiveSession persists entity graph`() = runTest {
        val focusDao = FakeFocusSessionDao()
        val repository = ActiveSessionRepositoryImpl(focusDao, FakeHistorySessionDao())
        val snapshot = sampleSession()

        repository.saveActiveSession(snapshot)

        assertTrue(focusDao.hasActiveSession())
        assertEquals(snapshot, repository.getActiveSession())
        assertEquals(snapshot.timeline.segments.size, focusDao.segments.size)
        assertEquals(snapshot.timeline.hourSplits.size, focusDao.hourSplits.size)
        assertEquals(1, focusDao.replaceSegmentsCount)
        assertEquals(1, focusDao.replaceHourSplitsCount)
    }

    @Test
    fun `updateActiveSession reuses existing session id`() = runTest {
        val focusDao = FakeFocusSessionDao()
        val repository = ActiveSessionRepositoryImpl(focusDao, FakeHistorySessionDao())
        val initial = sampleSession(totalCycle = 3)
        repository.saveActiveSession(initial)
        val existingId = focusDao.entity?.sessionId
        val updated = initial.copy(totalCycle = 4, elapsedPauseEpochMs = 1_000L)

        repository.updateActiveSession(updated)

        assertEquals(existingId, focusDao.entity?.sessionId)
        assertEquals(updated, repository.getActiveSession())
        assertEquals(2, focusDao.replaceSegmentsCount)
    }

    @Test
    fun `getActiveSession throws when nothing stored`() = runTest {
        val repository =
            ActiveSessionRepositoryImpl(FakeFocusSessionDao(), FakeHistorySessionDao())

        assertFailsWith<IllegalStateException> {
            repository.getActiveSession()
        }
    }

    @Test
    fun `hasActiveSession reflects dao state`() = runTest {
        val focusDao = FakeFocusSessionDao()
        val repository = ActiveSessionRepositoryImpl(focusDao, FakeHistorySessionDao())

        assertFalse(repository.hasActiveSession())

        repository.saveActiveSession(sampleSession())

        assertTrue(repository.hasActiveSession())
    }

    @Test
    fun `completeSession logs history and clears rows`() = runTest {
        val focusDao = FakeFocusSessionDao()
        val historyDao = FakeHistorySessionDao()
        val repository = ActiveSessionRepositoryImpl(focusDao, historyDao)
        val snapshot = sampleSession()

        repository.saveActiveSession(snapshot)
        repository.completeSession(snapshot)

        val history = historyDao.inserted
        assertEquals(snapshot.startedAtEpochMs, history?.dateStartedEpochMs)
        val totalDuration =
            snapshot.timeline.segments
                .filter { it.timerStatus != TimerStatusDomain.Initial }
                .sumOf { it.timer.durationEpochMs }
        assertEquals(snapshot.startedAtEpochMs + totalDuration, history?.dateFinishedEpochMs)
        assertNull(focusDao.entity)
        assertTrue(focusDao.segments.isEmpty())
        assertTrue(focusDao.hourSplits.isEmpty())
    }

    @Test
    fun `completeSession only counts elapsed segments`() = runTest {
        val focusDao = FakeFocusSessionDao()
        val historyDao = FakeHistorySessionDao()
        val repository = ActiveSessionRepositoryImpl(focusDao, historyDao)
        val focusMinutes = 25
        val breakMinutes = 5
        val snapshot = sampleSession(
            focusMinutes = focusMinutes,
            breakMinutes = breakMinutes,
        ).copy(
            timeline = TimelineDomain(
                segments =
                listOf(
                    TimerSegmentsDomain(
                        type = TimerType.FOCUS,
                        cycleNumber = 1,
                        timer = TimerDomain(durationEpochMs = focusMinutes * 60_000L),
                        timerStatus = TimerStatusDomain.Completed,
                    ),
                    TimerSegmentsDomain(
                        type = TimerType.SHORT_BREAK,
                        cycleNumber = 1,
                        timer = TimerDomain(durationEpochMs = breakMinutes * 60_000L),
                        timerStatus = TimerStatusDomain.Completed,
                    ),
                    TimerSegmentsDomain(
                        type = TimerType.FOCUS,
                        cycleNumber = 2,
                        timer = TimerDomain(durationEpochMs = focusMinutes * 60_000L),
                        timerStatus = TimerStatusDomain.Initial,
                    ),
                ),
                hourSplits = emptyList(),
            ),
        )

        repository.saveActiveSession(snapshot)
        repository.completeSession(snapshot)

        val history = historyDao.inserted ?: error("history not inserted")
        assertEquals(focusMinutes, history.totalFocusMinutes)
        assertEquals(breakMinutes, history.totalBreakMinutes)
        val elapsedMillis = (focusMinutes + breakMinutes) * 60_000L
        assertEquals(snapshot.startedAtEpochMs + elapsedMillis, history.dateFinishedEpochMs)
    }

    @Test
    fun `clearActiveSession wipes entity graph`() = runTest {
        val focusDao = FakeFocusSessionDao()
        val repository = ActiveSessionRepositoryImpl(focusDao, FakeHistorySessionDao())
        repository.saveActiveSession(sampleSession())
        val beforeClear = focusDao.clearCount

        repository.clearActiveSession()

        assertNull(focusDao.entity)
        assertTrue(focusDao.segments.isEmpty())
        assertTrue(focusDao.hourSplits.isEmpty())
        assertEquals(beforeClear + 1, focusDao.clearCount)
    }

    private fun sampleSession(
        totalCycle: Int = 2,
        startedAt: Long = 1_000L,
        focusMinutes: Int = 25,
        breakMinutes: Int = 5,
    ): PomodoroSessionDomain {
        val focusDuration = focusMinutes * 60_000L
        val breakDuration = breakMinutes * 60_000L
        val segments =
            listOf(
                TimerSegmentsDomain(
                    type = TimerType.FOCUS,
                    cycleNumber = 1,
                    timer =
                    TimerDomain(
                        durationEpochMs = focusDuration,
                        finishedInMillis = startedAt + focusDuration,
                    ),
                    timerStatus = TimerStatusDomain.Running,
                ),
                TimerSegmentsDomain(
                    type = TimerType.SHORT_BREAK,
                    cycleNumber = 1,
                    timer =
                    TimerDomain(
                        durationEpochMs = breakDuration,
                        finishedInMillis = startedAt + focusDuration + breakDuration,
                    ),
                    timerStatus = TimerStatusDomain.Initial,
                ),
            )
        return PomodoroSessionDomain(
            totalCycle = totalCycle,
            startedAtEpochMs = startedAt,
            elapsedPauseEpochMs = 0L,
            timeline = TimelineDomain(
                segments = segments,
                hourSplits = listOf(focusMinutes + breakMinutes),
            ),
            quote = QuoteContent.DEFAULT_QUOTE,
        )
    }

    private class FakeFocusSessionDao : FocusSessionDao {
        var entity: ActiveSessionEntity? = null
        val segments: MutableList<ActiveSessionSegmentEntity> = mutableListOf()
        val hourSplits: MutableList<ActiveSessionHourSplitEntity> = mutableListOf()
        var replaceSegmentsCount = 0
        var replaceHourSplitsCount = 0
        var clearCount = 0
        private var nextId = 1L

        override suspend fun hasActiveSession(): Boolean = entity != null

        override suspend fun getActiveSessionWithRelations(): ActiveSessionWithRelations? =
            entity?.let { stored ->
                ActiveSessionWithRelations(
                    session = stored,
                    segments = segments.toList(),
                    hourSplits = hourSplits.toList(),
                )
            }

        override suspend fun getActiveSessionId(): Long? = entity?.sessionId

        override suspend fun upsertActiveSession(entity: ActiveSessionEntity): Long {
            val assignedId =
                if (entity.sessionId == 0L) {
                    val id = nextId++
                    this.entity = entity.copy(sessionId = id)
                    id
                } else {
                    this.entity = entity
                    -1L
                }
            return assignedId
        }

        override suspend fun insertSegments(segments: List<ActiveSessionSegmentEntity>) {
            this.segments += segments
        }

        override suspend fun insertHourSplits(entities: List<ActiveSessionHourSplitEntity>) {
            hourSplits += entities
        }

        override suspend fun deleteSegments(sessionId: Long) {
            segments.removeAll { it.sessionId == sessionId }
        }

        override suspend fun deleteHourSplits(sessionId: Long) {
            hourSplits.removeAll { it.sessionId == sessionId }
        }

        override suspend fun replaceSegments(
            sessionId: Long,
            segments: List<ActiveSessionSegmentEntity>,
        ) {
            replaceSegmentsCount++
            deleteSegments(sessionId)
            insertSegments(segments)
        }

        override suspend fun replaceHourSplits(
            sessionId: Long,
            splits: List<ActiveSessionHourSplitEntity>,
        ) {
            replaceHourSplitsCount++
            deleteHourSplits(sessionId)
            insertHourSplits(splits)
        }

        override suspend fun clearActiveSession() {
            clearCount++
            entity = null
            segments.clear()
            hourSplits.clear()
        }
    }

    private class FakeHistorySessionDao : HistorySessionDao {
        var inserted: HistorySessionEntity? = null

        override suspend fun insertFinishedSession(entity: HistorySessionEntity) {
            inserted = entity
        }

        override suspend fun getSessionsBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): List<HistorySessionEntity> = emptyList()

        override suspend fun getTotalFocusMinutesBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): Int = 0

        override suspend fun getAvailableYears(): List<Int> = emptyList()
    }
}
