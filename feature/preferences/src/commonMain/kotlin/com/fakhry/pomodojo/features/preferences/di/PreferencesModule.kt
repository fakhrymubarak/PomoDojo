package com.fakhry.pomodojo.features.preferences.di

import com.fakhry.pomodojo.features.preferences.data.repository.InitPreferencesRepositoryImpl
import com.fakhry.pomodojo.features.preferences.data.repository.PreferencesRepositoryImpl
import com.fakhry.pomodojo.features.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.features.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.features.preferences.domain.usecase.InitPreferencesRepository
import com.fakhry.pomodojo.features.preferences.domain.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.features.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.features.preferences.ui.PreferencesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val preferencesModule = module {
    viewModelOf(::PreferencesViewModel)
    factoryOf(::PreferenceCascadeResolver)
    factoryOf(::BuildTimerSegmentsUseCase)
    factoryOf(::BuildHourSplitTimelineUseCase)
    singleOf(::PreferencesRepositoryImpl) bind PreferencesRepository::class
    singleOf(::InitPreferencesRepositoryImpl) bind InitPreferencesRepository::class
}
