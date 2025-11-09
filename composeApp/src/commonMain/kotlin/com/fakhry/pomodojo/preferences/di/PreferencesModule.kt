package com.fakhry.pomodojo.preferences.di

import com.fakhry.pomodojo.preferences.data.repository.PreferencesRepositoryImpl
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferenceCascadeResolver
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.preferences.ui.PreferencesViewModel
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
}
