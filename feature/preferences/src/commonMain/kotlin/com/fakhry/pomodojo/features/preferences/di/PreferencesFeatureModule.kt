package com.fakhry.pomodojo.features.preferences.di

import com.fakhry.pomodojo.features.preferences.data.repository.InitPreferencesRepositoryImpl
import com.fakhry.pomodojo.features.preferences.domain.repository.InitPreferencesRepository
import com.fakhry.pomodojo.features.preferences.ui.PreferencesViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val preferencesFeatureModule = module {
    viewModelOf(::PreferencesViewModel)
    singleOf(::InitPreferencesRepositoryImpl) bind InitPreferencesRepository::class
}
