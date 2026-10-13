package com.fakhry.pomodojo.data.preferences.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.fakhry.pomodojo.core.datastore.PreferenceKeys
import com.fakhry.pomodojo.domain.preferences.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InitPreferencesRepositoryImplTest {
    private val dataStore = InitInMemoryPreferencesDataStore()
    private val repository = InitPreferencesRepositoryImpl(dataStore)

    @Test
    fun `updateAppTheme persists selection`() = runTest {
        repository.updateAppTheme(AppTheme.LIGHT)

        val updated = repository.initPreferences.first()
        assertEquals(AppTheme.LIGHT.storageValue, updated.appTheme)
    }

    @Test
    fun `hasActiveSession follows the stored session`() = runTest {
        assertFalse(repository.initPreferences.first().hasActiveSession)

        dataStore.edit { it[PreferenceKeys.ACTIVE_SESSION_KEY] = "{}" }
        assertTrue(repository.initPreferences.first().hasActiveSession)

        dataStore.edit { it.remove(PreferenceKeys.ACTIVE_SESSION_KEY) }
        assertFalse(repository.initPreferences.first().hasActiveSession)
    }
}

private class InitInMemoryPreferencesDataStore(
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
