package com.fakhry.pomodojo.features.focus.data.repository

import com.fakhry.pomodojo.core.datastore.PreferenceStorage
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import com.fakhry.pomodojo.shared.domain.model.quote.QuoteContent
import com.fakhry.pomodojo.shared.domain.model.timeline.TimelineDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActiveSessionRepositoryImplTest {
    @Test
    fun `saveActiveSession persists snapshot as json`() = runTest {
        val dispatcher = DispatcherProvider(StandardTestDispatcher(testScheduler))
        val prefStorage = FakePreferenceStorage()
        val repository = ActiveSessionRepositoryImpl(prefStorage, dispatcher)
        val snapshot = sampleSession()

        repository.saveActiveSession(snapshot)

        assertEquals(snapshot, repository.getActiveSession())
        assertTrue(repository.hasActiveSession())
    }

    @Test
    fun `updateActiveSession overwrites stored snapshot`() = runTest {
        val dispatcher = DispatcherProvider(StandardTestDispatcher(testScheduler))
        val prefStorage = FakePreferenceStorage()
        val repository = ActiveSessionRepositoryImpl(prefStorage, dispatcher)
        val initial = sampleSession(totalCycle = 3)
        repository.saveActiveSession(initial)
        val updated = initial.copy(totalCycle = 4, elapsedPauseEpochMs = 1_000L)

        repository.updateActiveSession(updated)

        assertEquals(updated, repository.getActiveSession())
    }

    @Test
    fun `getActiveSession throws when nothing stored`() = runTest {
        val dispatcher = DispatcherProvider(StandardTestDispatcher(testScheduler))
        val prefStorage = FakePreferenceStorage()
        val repository = ActiveSessionRepositoryImpl((prefStorage), dispatcher)

        assertEquals(repository.getActiveSession(), PomodoroSessionDomain())
    }

    @Test
    fun `hasActiveSession reflects data store state`() = runTest {
        val dispatcher = DispatcherProvider(StandardTestDispatcher(testScheduler))
        val prefStorage = FakePreferenceStorage()
        val repository = ActiveSessionRepositoryImpl(prefStorage, dispatcher)

        assertFalse(repository.hasActiveSession())

        repository.saveActiveSession(sampleSession())

        assertTrue(repository.hasActiveSession())
    }

    @Test
    fun `completeSession clears stored snapshot`() = runTest {
        val dispatcher = DispatcherProvider(StandardTestDispatcher(testScheduler))
        val prefStorage = FakePreferenceStorage()
        val repository = ActiveSessionRepositoryImpl(prefStorage, dispatcher)
        val snapshot = sampleSession()

        repository.saveActiveSession(snapshot)
        repository.completeSession(snapshot)

        assertFalse(repository.hasActiveSession())
        assertEquals(repository.getActiveSession(), PomodoroSessionDomain())
    }

    @Test
    fun `clearActiveSession wipes stored json`() = runTest {
        val dispatcher = DispatcherProvider(StandardTestDispatcher(testScheduler))
        val prefStorage = FakePreferenceStorage()
        val repository = ActiveSessionRepositoryImpl(prefStorage, dispatcher)
        repository.saveActiveSession(sampleSession())

        repository.clearActiveSession()

        assertFalse(repository.hasActiveSession())
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
}

private class FakePreferenceStorage : PreferenceStorage {
    private val pomodoroState = MutableStateFlow(PomodoroSessionDomain())

    override val preferences:
        Flow<com.fakhry.pomodojo.shared.domain.model.preferences.PomodoroPreferences>
        get() = MutableStateFlow(
            com.fakhry.pomodojo.shared.domain.model.preferences.PomodoroPreferences(),
        )
    override val initPreferences:
        Flow<com.fakhry.pomodojo.shared.domain.model.preferences.InitAppPreferences>
        get() = MutableStateFlow(
            com.fakhry.pomodojo.shared.domain.model.preferences.InitAppPreferences(),
        )
    override val activeSession: Flow<PomodoroSessionDomain> = pomodoroState

    override suspend fun updatePreferences(
        transform: (
            com.fakhry.pomodojo.shared.domain.model.preferences.PomodoroPreferences,
        ) -> com.fakhry.pomodojo.shared.domain.model.preferences.PomodoroPreferences,
    ) = Unit

    override suspend fun updateInitPreferences(
        transform: (
            com.fakhry.pomodojo.shared.domain.model.preferences.InitAppPreferences,
        ) -> com.fakhry.pomodojo.shared.domain.model.preferences.InitAppPreferences,
    ) = Unit

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        pomodoroState.update { snapshot }
    }

    override suspend fun clearActiveSession() {
        pomodoroState.update { PomodoroSessionDomain() }
    }
}
