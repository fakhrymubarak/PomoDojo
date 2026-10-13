package com.fakhry.pomodojo.data.pomodoro.repository

import androidx.datastore.preferences.core.edit
import com.fakhry.pomodojo.core.datastore.InMemoryPreferencesDataStore
import com.fakhry.pomodojo.core.datastore.PreferenceKeys
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

/**
 * Pins the JSON shape of the stored active Session. Installed apps may hold a Session in this
 * shape across an upgrade, so a change here needs a migration, not just a new expectation.
 */
class StoredSessionFormatTest {
    @Test
    fun `stored session in the released format restores`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        dataStore.edit { it[PreferenceKeys.ACTIVE_SESSION_KEY] = STORED_JSON }

        val restored = ActiveSessionStoreImpl(dataStore).session.first()

        assertEquals(storedSession, restored)
    }

    @Test
    fun `saving a session writes the released format`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()

        ActiveSessionStoreImpl(dataStore).update { storedSession }

        assertEquals(STORED_JSON, dataStore.data.first()[PreferenceKeys.ACTIVE_SESSION_KEY])
    }

    @Test
    fun `undecodable stored session reads as no session`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        dataStore.edit {
            it[PreferenceKeys.ACTIVE_SESSION_KEY] =
                """{"timeline":{"segments":[{"type":"NAP"}]}}"""
        }
        assertNull(ActiveSessionStoreImpl(dataStore).session.first())
    }
}

private const val STORED_JSON =
    """{"totalCycle":1,"startedAtEpochMs":1000,"elapsedPauseEpochMs":0,""" +
        """"timeline":{"segments":[""" +
        """{"type":"FOCUS","cycleNumber":1,""" +
        """"timer":{"progress":0.5,"durationEpochMs":60000,""" +
        """"finishedInMillis":61000,"startedPauseTime":31000,"elapsedPauseTime":0},""" +
        """"timerStatus":"PAUSED"},""" +
        """{"type":"SHORT_BREAK","cycleNumber":1,""" +
        """"timer":{"progress":0.0,"durationEpochMs":60000,""" +
        """"finishedInMillis":0,"startedPauseTime":0,"elapsedPauseTime":0},""" +
        """"timerStatus":"INITIAL"}""" +
        """],"hourSplits":[2]},""" +
        """"quote":{"id":"q","text":"Focus","character":"Sensei","sourceTitle":null,"metadata":null}}"""

private val storedSession = PomodoroSessionDomain(
    totalCycle = 1,
    startedAtEpochMs = 1_000L,
    elapsedPauseEpochMs = 0L,
    timeline = TimelineDomain(
        segments = listOf(
            TimerSegmentsDomain(
                type = TimerType.FOCUS,
                cycleNumber = 1,
                timer = TimerDomain(
                    progress = 0.5f,
                    durationEpochMs = 60_000L,
                    finishedInMillis = 61_000L,
                    startedPauseTime = 31_000L,
                ),
                timerStatus = TimerStatusDomain.PAUSED,
            ),
            TimerSegmentsDomain(
                type = TimerType.SHORT_BREAK,
                cycleNumber = 1,
                timer = TimerDomain(durationEpochMs = 60_000L),
                timerStatus = TimerStatusDomain.INITIAL,
            ),
        ),
        hourSplits = listOf(2),
    ),
    quote = QuoteContent(id = "q", text = "Focus", character = "Sensei"),
)
