package com.fakhry.pomodojo.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.fakhry.pomodojo.shared.domain.model.preferences.AppTheme
import com.fakhry.pomodojo.shared.domain.model.preferences.PomodoroPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DataStorePreferenceStorageTest {
    @Test
    fun `initial pomodoro preferences return default values`() = runTest {
        val storage = DataStorePreferenceStorage(FakeDataStore())

        val preferences = storage.preferences.first()

        assertEquals(PomodoroPreferences.Companion.DEFAULT_REPEAT_COUNT, preferences.repeatCount)
        assertEquals(PomodoroPreferences.Companion.DEFAULT_FOCUS_MINUTES, preferences.focusMinutes)
        assertEquals(PomodoroPreferences.Companion.DEFAULT_BREAK_MINUTES, preferences.breakMinutes)
        assertTrue(preferences.longBreakEnabled)
        assertEquals(
            PomodoroPreferences.Companion.DEFAULT_LONG_BREAK_AFTER,
            preferences.longBreakAfter,
        )
        assertEquals(
            PomodoroPreferences.Companion.DEFAULT_LONG_BREAK_MINUTES,
            preferences.longBreakMinutes,
        )
        assertFalse(preferences.alwaysOnDisplayEnabled)
    }

    @Test
    fun `initial init preferences return default values`() = runTest {
        val storage = DataStorePreferenceStorage(FakeDataStore())

        val initPreferences = storage.initPreferences.first()

        assertEquals(AppTheme.DARK, initPreferences.appTheme)
        assertFalse(initPreferences.hasActiveSession)
    }

    @Test
    fun `updatePreferences modifies pomodoro preferences`() = runTest {
        val storage = DataStorePreferenceStorage(FakeDataStore())

        storage.updatePreferences { current ->
            current.copy(focusMinutes = 50, breakMinutes = 10)
        }

        val updated = storage.preferences.first()
        assertEquals(50, updated.focusMinutes)
        assertEquals(10, updated.breakMinutes)
    }

    @Test
    fun `updateInitPreferences modifies init values`() = runTest {
        val storage = DataStorePreferenceStorage(FakeDataStore())

        storage.updateInitPreferences { current ->
            current.copy(appTheme = AppTheme.LIGHT, hasActiveSession = true)
        }

        val updated = storage.initPreferences.first()
        assertEquals(AppTheme.LIGHT, updated.appTheme)
        assertTrue(updated.hasActiveSession)
    }

    @Test
    fun `preferences flow emits distinct values only`() = runTest {
        val storage = DataStorePreferenceStorage(FakeDataStore())

        val emissions = mutableListOf<PomodoroPreferences>()

        emissions.add(storage.preferences.first())
        storage.updatePreferences {
            it.copy(focusMinutes = PomodoroPreferences.Companion.DEFAULT_FOCUS_MINUTES)
        }
        storage.updatePreferences { it.copy(focusMinutes = 50) }
        emissions.add(storage.preferences.first())
        storage.updatePreferences { it.copy(focusMinutes = 50) }

        assertEquals(2, emissions.size)
        assertEquals(PomodoroPreferences.Companion.DEFAULT_FOCUS_MINUTES, emissions[0].focusMinutes)
        assertEquals(50, emissions[1].focusMinutes)
    }

    private class FakeDataStore : DataStore<Preferences> {
        private val state = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = state

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }
}
