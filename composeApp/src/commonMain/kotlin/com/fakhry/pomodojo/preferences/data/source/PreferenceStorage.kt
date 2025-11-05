package com.fakhry.pomodojo.preferences.data.source

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.flow.Flow

interface PreferenceStorage {
    val preferences: Flow<PreferencesDomain>
    suspend fun update(transform: (PreferencesDomain) -> PreferencesDomain)
}