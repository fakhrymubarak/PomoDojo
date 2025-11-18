package com.fakhry.pomodojo.features.preferences.data.repository

import com.fakhry.pomodojo.features.preferences.domain.model.AppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InitPreferencesRepositoryImplTest {
    private val storage = FakePreferenceStorage()
    private val repository = InitPreferencesRepositoryImpl(storage)

    @Test
    fun `updateAppTheme persists selection`() = runTest {
        repository.updateAppTheme(AppTheme.LIGHT)

        val updated = storage.initPreferences.first()
        assertEquals(AppTheme.LIGHT, updated.appTheme)
    }

    @Test
    fun `updateHasActiveSession toggles state`() = runTest {
        repository.updateHasActiveSession(true)

        assertTrue(storage.initPreferences.first().hasActiveSession)
    }
}
