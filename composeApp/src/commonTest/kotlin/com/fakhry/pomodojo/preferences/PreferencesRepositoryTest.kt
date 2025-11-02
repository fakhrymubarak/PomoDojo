package com.fakhry.pomodojo.preferences

import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.preferences.domain.PomodoroPreferences
import com.fakhry.pomodojo.preferences.domain.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.domain.PreferencesValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreferencesRepositoryTest {

    private val storage = FakePreferenceStorage()
    private val repository = PreferencesRepository(
        storage = storage,
        cascadeResolver = PreferenceCascadeResolver(),
        validator = PreferencesValidator,
    )

    @Test
    fun `update focus minutes cascades dependent values`() = runTest {
        repository.updateFocusMinutes(50)

        val updated = storage.preferences.first()
        assertEquals(50, updated.focusMinutes)
        assertEquals(10, updated.breakMinutes)
        assertEquals(2, updated.longBreakAfter)
        assertEquals(20, updated.longBreakMinutes)
    }

    @Test
    fun `update break minutes cascades long break minutes`() = runTest {
        repository.updateBreakMinutes(2)

        val updated = storage.preferences.first()
        assertEquals(2, updated.breakMinutes)
        assertEquals(4, updated.longBreakMinutes)
    }

    @Test
    fun `toggle long break persists state`() = runTest {
        repository.updateLongBreakEnabled(false)

        val updated = storage.preferences.first()
        assertFalse(updated.longBreakEnabled)

        repository.updateLongBreakEnabled(true)
        val restored = storage.preferences.first()
        assertTrue(restored.longBreakEnabled)
    }

    @Test
    fun `invalid repeat count throws`() = runTest {
        var thrown = false
        try {
            repository.updateRepeatCount(1)
        } catch (_: IllegalArgumentException) {
            thrown = true
        }

        assertTrue(thrown)
        val current = storage.preferences.first()
        assertEquals(PomodoroPreferences.DEFAULT_REPEAT_COUNT, current.repeatCount)
    }

    private class FakePreferenceStorage :
        PreferenceStorage {

        private val state = MutableStateFlow(PomodoroPreferences())

        override val preferences: Flow<PomodoroPreferences> = state

        override suspend fun update(transform: (PomodoroPreferences) -> PomodoroPreferences) {
            state.update(transform)
        }
    }
}
