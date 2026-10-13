package com.fakhry.pomodojo.data.pomodoro.repository

import com.fakhry.pomodojo.core.datastore.InMemoryPreferencesDataStore
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActiveSessionStoreImplTest {
    @Test
    fun `update stores the session and session reads it back`() = runTest {
        val store = ActiveSessionStoreImpl(InMemoryPreferencesDataStore())
        val snapshot = sampleSession()

        assertEquals(snapshot, store.update { snapshot })

        assertEquals(snapshot, store.session.first())
    }

    @Test
    fun `nothing stored reads as no session`() = runTest {
        val store = ActiveSessionStoreImpl(InMemoryPreferencesDataStore())

        assertNull(store.session.first())
    }

    @Test
    fun `update transforms what is stored`() = runTest {
        val store = ActiveSessionStoreImpl(InMemoryPreferencesDataStore())
        store.update { sampleSession(totalCycle = 2) }

        val updated = store.update { it?.copy(totalCycle = it.totalCycle + 1) }

        assertEquals(3, updated?.totalCycle)
        assertEquals(3, store.session.first()?.totalCycle)
    }

    @Test
    fun `update to null clears the session`() = runTest {
        val store = ActiveSessionStoreImpl(InMemoryPreferencesDataStore())
        store.update { sampleSession() }

        store.update { null }

        assertNull(store.session.first())
    }

    @Test
    fun `take hands the session out once`() = runTest {
        val store = ActiveSessionStoreImpl(InMemoryPreferencesDataStore())
        val snapshot = sampleSession()
        store.update { snapshot }

        assertEquals(snapshot, store.take())
        assertNull(store.take())
        assertNull(store.session.first())
    }
}

private fun sampleSession(
    totalCycle: Int = 2,
    startedAt: Long = 1_000L,
    focusMinutes: Int = 25,
    breakMinutes: Int = 5,
): PomodoroSessionDomain {
    val focusDuration = focusMinutes * 60_000L
    val breakDuration = breakMinutes * 60_000L
    val segments = listOf(
        TimerSegmentsDomain(
            type = TimerType.FOCUS,
            cycleNumber = 1,
            timer = TimerDomain(
                durationEpochMs = focusDuration,
                finishedInMillis = startedAt + focusDuration,
            ),
            timerStatus = TimerStatusDomain.RUNNING,
        ),
        TimerSegmentsDomain(
            type = TimerType.SHORT_BREAK,
            cycleNumber = 1,
            timer = TimerDomain(
                durationEpochMs = breakDuration,
                finishedInMillis = startedAt + focusDuration + breakDuration,
            ),
            timerStatus = TimerStatusDomain.INITIAL,
        ),
    )
    return PomodoroSessionDomain(
        totalCycle = totalCycle,
        startedAtEpochMs = startedAt,
        timeline = TimelineDomain(
            segments = segments,
            hourSplits = listOf(focusMinutes + breakMinutes),
        ),
        quote = QuoteContent.DEFAULT_QUOTE,
    )
}
