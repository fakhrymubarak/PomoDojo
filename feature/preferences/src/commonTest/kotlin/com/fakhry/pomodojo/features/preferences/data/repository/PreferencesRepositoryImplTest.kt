package com.fakhry.pomodojo.features.preferences.data.repository

import com.fakhry.pomodojo.domain.preferences.usecase.PreferenceCascadeResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreferencesRepositoryImplTest {
    private val storage = FakePreferenceStorage()
    private val repository = PreferencesRepositoryImpl(
        storage = storage,
        cascadeResolver = PreferenceCascadeResolver(),
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
}
