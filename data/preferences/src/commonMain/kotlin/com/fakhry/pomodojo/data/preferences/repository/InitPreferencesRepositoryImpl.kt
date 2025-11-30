package com.fakhry.pomodojo.data.preferences.repository

import com.fakhry.pomodojo.core.datastore.PreferenceStorage
import com.fakhry.pomodojo.domain.preferences.model.AppTheme
import com.fakhry.pomodojo.domain.preferences.repository.InitPreferencesRepository
import kotlinx.coroutines.flow.distinctUntilChanged

class InitPreferencesRepositoryImpl(
    private val storage: PreferenceStorage,
) : InitPreferencesRepository {
    override val initPreferences = storage.initPreferences.distinctUntilChanged()

    override suspend fun updateAppTheme(theme: AppTheme) {
        storage.updateInitPreferences { it.copy(appTheme = theme) }
    }

    override suspend fun updateHasActiveSession(value: Boolean) {
        storage.updateInitPreferences { it.copy(hasActiveSession = value) }
    }
}
