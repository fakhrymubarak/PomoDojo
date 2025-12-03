package com.fakhry.pomodojo.data.preferences.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.fakhry.pomodojo.domain.preferences.usecase.PreferenceCascadeResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreferencesRepositoryImplTest {
    private val dataStore = InMemoryPreferencesDataStore()
    private val repository = PreferencesRepositoryImpl(
        dataStore = dataStore,
        cascadeResolver = PreferenceCascadeResolver(),
    )

    @Test
    fun `update focus minutes cascades dependent values`() = runTest {
        repository.updateFocusMinutes(50)

        val updated = repository.preferences.first()
        assertEquals(50, updated.focusMinutes)
        assertEquals(10, updated.breakMinutes)
        assertEquals(2, updated.longBreakAfter)
        assertEquals(20, updated.longBreakMinutes)
    }

    @Test
    fun `update break minutes cascades long break minutes`() = runTest {
        repository.updateBreakMinutes(2)

        val updated = repository.preferences.first()
        assertEquals(2, updated.breakMinutes)
        assertEquals(4, updated.longBreakMinutes)
    }

    @Test
    fun `toggle long break persists state`() = runTest {
        repository.updateLongBreakEnabled(false)

        val updated = repository.preferences.first()
        assertFalse(updated.longBreakEnabled)

        repository.updateLongBreakEnabled(true)
        val restored = repository.preferences.first()
        assertTrue(restored.longBreakEnabled)
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
