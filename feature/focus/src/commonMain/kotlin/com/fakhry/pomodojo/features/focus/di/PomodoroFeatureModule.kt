package com.fakhry.pomodojo.features.focus.di

import com.fakhry.pomodojo.core.utils.date.CurrentTimeProvider
import com.fakhry.pomodojo.core.utils.date.SystemCurrentTimeProvider
import com.fakhry.pomodojo.features.focus.data.repository.StaticQuoteRepository
import com.fakhry.pomodojo.features.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.features.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.features.focus.ui.viewmodel.PomodoroSessionViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val pomodoroFeatureModule = module {
    viewModelOf(::PomodoroSessionViewModel)
    singleOf(::StaticQuoteRepository) bind QuoteRepository::class
    singleOf(::CreatePomodoroSessionUseCase)
    single<CurrentTimeProvider> { SystemCurrentTimeProvider }
}
