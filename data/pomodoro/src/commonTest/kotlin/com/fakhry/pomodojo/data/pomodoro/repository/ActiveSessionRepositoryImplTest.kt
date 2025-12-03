package com.fakhry.pomodojo.data.pomodoro.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActiveSessionRepositoryImplTest {
    @Test
    fun `saveActiveSession persists and restores snapshot`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val repository = ActiveSessionRepositoryImpl(dataStore)
        val snapshot = sampleSession()

        repository.saveActiveSession(snapshot)

        val restored = repository.getActiveSession()
        assertEquals(snapshot, restored)
        assertTrue(repository.hasActiveSession())
    }

    @Test
    fun `getActiveSession returns default when nothing stored`() = runTest {
        val repository = ActiveSessionRepositoryImpl(InMemoryPreferencesDataStore())

        val restored = repository.getActiveSession()

        assertEquals(PomodoroSessionDomain(), restored)
        assertFalse(repository.hasActiveSession())
    }

    @Test
    fun `clearActiveSession removes persisted snapshot`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val repository = ActiveSessionRepositoryImpl(dataStore)
        val snapshot = sampleSession()
        repository.saveActiveSession(snapshot)

        repository.clearActiveSession()

        assertFalse(repository.hasActiveSession())
        assertEquals(PomodoroSessionDomain(), repository.getActiveSession())
    }
}

private class InMemoryPreferencesDataStore(
    initial: Preferences = emptyPreferences(),
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> = state

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
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
        elapsedPauseEpochMs = 0L,
        timeline = TimelineDomain(
            segments = segments,
            hourSplits = listOf(focusMinutes + breakMinutes),
        ),
        quote = QuoteContent.DEFAULT_QUOTE,
    )
}
