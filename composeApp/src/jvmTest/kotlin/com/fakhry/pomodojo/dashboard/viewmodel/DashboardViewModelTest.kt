package com.fakhry.pomodojo.dashboard.viewmodel

import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepository
import com.fakhry.pomodojo.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.PreferenceCascadeResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state uses stored focus minutes`() = runTest {
        val initialPreferences = PreferencesDomain(focusMinutes = 30)
        val repository = createRepository(initialPreferences)

        val viewModel = DashboardViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(30, viewModel.state.value.timerMinutes)
    }

    @Test
    fun `timer updates when focus minutes change`() = runTest {
        val initialPreferences = PreferencesDomain(focusMinutes = 25)
        val fakeStorage = FakePreferenceStorage(initialPreferences)
        val repository = PreferencesRepository(fakeStorage, PreferenceCascadeResolver())

        val viewModel = DashboardViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        val updated = initialPreferences.copy(focusMinutes = 45)
        fakeStorage.emit(updated)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(45, viewModel.state.value.timerMinutes)
    }

    private fun createRepository(initialPreferences: PreferencesDomain): PreferencesRepository {
        val storage = FakePreferenceStorage(initialPreferences)
        return PreferencesRepository(storage, PreferenceCascadeResolver())
    }

    private class FakePreferenceStorage(initial: PreferencesDomain) : PreferenceStorage {
        private val state = MutableStateFlow(initial)

        override val preferences = state.asStateFlow()

        override suspend fun update(transform: (PreferencesDomain) -> PreferencesDomain) {
            state.value = transform(state.value)
        }

        fun emit(preferences: PreferencesDomain) {
            state.value = preferences
        }
    }
}
