package com.fakhry.pomodojo.features.preferences.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.features.preferences.domain.model.PreferencesDomain
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
    fun `initial preferences returns default values`() = runTest {
        val dataStore = FakeDataStore()
        val storage = DataStorePreferenceStorage(dataStore)

        val preferences = storage.preferences.first()

        assertEquals(PreferencesDomain.DEFAULT_REPEAT_COUNT, preferences.repeatCount)
        assertEquals(PreferencesDomain.DEFAULT_FOCUS_MINUTES, preferences.focusMinutes)
        assertEquals(PreferencesDomain.DEFAULT_BREAK_MINUTES, preferences.breakMinutes)
        assertTrue(preferences.longBreakEnabled)
        assertEquals(PreferencesDomain.DEFAULT_LONG_BREAK_AFTER, preferences.longBreakAfter)
        assertEquals(PreferencesDomain.DEFAULT_LONG_BREAK_MINUTES, preferences.longBreakMinutes)
        assertEquals(AppTheme.DARK, preferences.appTheme)
        assertFalse(preferences.alwaysOnDisplayEnabled)
    }

    @Test
    fun `update modifies preferences`() = runTest {
        val dataStore = FakeDataStore()
        val storage = DataStorePreferenceStorage(dataStore)

        storage.update { current ->
            current.copy(focusMinutes = 50, breakMinutes = 10)
        }

        val updated = storage.preferences.first()
        assertEquals(50, updated.focusMinutes)
        assertEquals(10, updated.breakMinutes)
    }

    @Test
    fun `update persists all preference fields`() = runTest {
        val dataStore = FakeDataStore()
        val storage = DataStorePreferenceStorage(dataStore)

        storage.update { current ->
            current.copy(
                repeatCount = 6,
                focusMinutes = 30,
                breakMinutes = 7,
                longBreakEnabled = false,
                longBreakAfter = 3,
                longBreakMinutes = 25,
                appTheme = AppTheme.DARK,
                alwaysOnDisplayEnabled = true,
            )
        }

        val updated = storage.preferences.first()
        assertEquals(6, updated.repeatCount)
        assertEquals(30, updated.focusMinutes)
        assertEquals(7, updated.breakMinutes)
        assertFalse(updated.longBreakEnabled)
        assertEquals(3, updated.longBreakAfter)
        assertEquals(25, updated.longBreakMinutes)
        assertEquals(AppTheme.DARK, updated.appTheme)
        assertTrue(updated.alwaysOnDisplayEnabled)
    }

    @Test
    fun `multiple updates are applied sequentially`() = runTest {
        val dataStore = FakeDataStore()
        val storage = DataStorePreferenceStorage(dataStore)

        storage.update { it.copy(focusMinutes = 25) }
        storage.update { it.copy(breakMinutes = 5) }
        storage.update { it.copy(repeatCount = 3) }

        val updated = storage.preferences.first()
        assertEquals(25, updated.focusMinutes)
        assertEquals(5, updated.breakMinutes)
        assertEquals(3, updated.repeatCount)
    }

    @Test
    fun `update receives current state as input`() = runTest {
        val dataStore = FakeDataStore()
        val storage = DataStorePreferenceStorage(dataStore)

        storage.update { it.copy(focusMinutes = 50) }
        storage.update { current ->
            // Verify we get the updated value
            assertEquals(50, current.focusMinutes)
            current.copy(breakMinutes = current.focusMinutes / 5)
        }

        val updated = storage.preferences.first()
        assertEquals(50, updated.focusMinutes)
        assertEquals(10, updated.breakMinutes)
    }

    @Test
    fun `theme preference is persisted correctly`() = runTest {
        val dataStore = FakeDataStore()
        val storage = DataStorePreferenceStorage(dataStore)

        storage.update { it.copy(appTheme = AppTheme.LIGHT) }
        assertEquals(AppTheme.LIGHT, storage.preferences.first().appTheme)

        storage.update { it.copy(appTheme = AppTheme.DARK) }
        assertEquals(AppTheme.DARK, storage.preferences.first().appTheme)
    }

    @Test
    fun `always on display toggle is persisted`() = runTest {
        val dataStore = FakeDataStore()
        val storage = DataStorePreferenceStorage(dataStore)

        assertFalse(storage.preferences.first().alwaysOnDisplayEnabled)

        storage.update { it.copy(alwaysOnDisplayEnabled = true) }
        assertTrue(storage.preferences.first().alwaysOnDisplayEnabled)

        storage.update { it.copy(alwaysOnDisplayEnabled = false) }
        assertFalse(storage.preferences.first().alwaysOnDisplayEnabled)
    }

    @Test
    fun `long break enabled toggle is persisted`() = runTest {
        val dataStore = FakeDataStore()
        val storage = DataStorePreferenceStorage(dataStore)

        assertTrue(storage.preferences.first().longBreakEnabled)

        storage.update { it.copy(longBreakEnabled = false) }
        assertFalse(storage.preferences.first().longBreakEnabled)

        storage.update { it.copy(longBreakEnabled = true) }
        assertTrue(storage.preferences.first().longBreakEnabled)
    }

    @Test
    fun `preferences flow emits distinct values only`() = runTest {
        val dataStore = FakeDataStore()
        val storage = DataStorePreferenceStorage(dataStore)

        val emissions = mutableListOf<PreferencesDomain>()

        // Collect first emission
        emissions.add(storage.preferences.first())

        // Update with same value
        storage.update { it.copy(focusMinutes = PreferencesDomain.DEFAULT_FOCUS_MINUTES) }

        // Update with different value
        storage.update { it.copy(focusMinutes = 50) }
        emissions.add(storage.preferences.first())

        // Update with same value again
        storage.update { it.copy(focusMinutes = 50) }

        // Verify we got the distinct values
        assertEquals(2, emissions.size)
        assertEquals(PreferencesDomain.DEFAULT_FOCUS_MINUTES, emissions[0].focusMinutes)
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
