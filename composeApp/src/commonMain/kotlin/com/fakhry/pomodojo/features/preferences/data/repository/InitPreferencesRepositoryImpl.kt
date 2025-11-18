package com.fakhry.pomodojo.features.preferences.data.repository

import com.fakhry.pomodojo.core.datastore.PreferenceStorage
import com.fakhry.pomodojo.features.preferences.domain.usecase.InitPreferencesRepository
import com.fakhry.pomodojo.shared.domain.model.preferences.AppTheme
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
