package com.fakhry.pomodojo.features.focus.di

import com.fakhry.pomodojo.core.utils.date.CurrentTimeProvider
import com.fakhry.pomodojo.core.utils.date.SystemCurrentTimeProvider
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionRepository
import com.fakhry.pomodojo.feature.notification.audio.SoundPlayer
import com.fakhry.pomodojo.feature.notification.audio.provideSoundPlayer
import com.fakhry.pomodojo.feature.notification.notifications.PomodoroSessionNotifier
import com.fakhry.pomodojo.feature.notification.notifications.providePomodoroSessionNotifier
import com.fakhry.pomodojo.features.focus.data.repository.ActiveSessionRepositoryImpl
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
    single<ActiveSessionRepository> {
        ActiveSessionRepositoryImpl(get(), get())
    }
    singleOf(::StaticQuoteRepository) bind QuoteRepository::class
    singleOf(::CreatePomodoroSessionUseCase)
    single<PomodoroSessionNotifier> { providePomodoroSessionNotifier() }
    single<SoundPlayer> { provideSoundPlayer() }
    single<CurrentTimeProvider> { SystemCurrentTimeProvider }
}
