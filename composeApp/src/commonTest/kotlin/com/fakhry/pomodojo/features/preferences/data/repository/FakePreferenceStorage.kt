package com.fakhry.pomodojo.features.preferences.data.repository

import com.fakhry.pomodojo.features.preferences.data.source.PreferenceStorage
import com.fakhry.pomodojo.features.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakePreferenceStorage : PreferenceStorage {
    private val state = MutableStateFlow(PreferencesDomain())

    override val preferences: Flow<PreferencesDomain> = state

    override suspend fun update(transform: (PreferencesDomain) -> PreferencesDomain) {
        state.update(transform)
    }
}
