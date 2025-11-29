package com.fakhry.pomodojo.features.focus.di

import com.fakhry.pomodojo.core.database.PomoDojoRoomDatabase
import com.fakhry.pomodojo.core.database.createDatabase
import com.fakhry.pomodojo.core.framework.audio.SoundPlayer
import com.fakhry.pomodojo.core.framework.audio.provideSoundPlayer
import com.fakhry.pomodojo.core.framework.datetime.CurrentTimeProvider
import com.fakhry.pomodojo.core.framework.datetime.SystemCurrentTimeProvider
import com.fakhry.pomodojo.core.framework.notifications.PomodoroSessionNotifier
import com.fakhry.pomodojo.core.framework.notifications.providePomodoroSessionNotifier
import com.fakhry.pomodojo.features.focus.data.repository.ActiveSessionRepositoryImpl
import com.fakhry.pomodojo.features.focus.data.repository.HistorySessionRepositoryImpl
import com.fakhry.pomodojo.features.focus.data.repository.StaticQuoteRepository
import com.fakhry.pomodojo.features.focus.domain.repository.ActiveSessionRepository
import com.fakhry.pomodojo.features.focus.domain.repository.HistorySessionRepository
import com.fakhry.pomodojo.features.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.features.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.features.focus.ui.viewmodel.PomodoroSessionViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val focusModule = module {
    viewModelOf(::PomodoroSessionViewModel)
    single<ActiveSessionRepository> {
        ActiveSessionRepositoryImpl(get(), get())
    }
    singleOf(::HistorySessionRepositoryImpl) bind HistorySessionRepository::class
    singleOf(::StaticQuoteRepository) bind QuoteRepository::class
    singleOf(::CreatePomodoroSessionUseCase)
    single<PomoDojoRoomDatabase> { createDatabase() }
    single<PomodoroSessionNotifier> { providePomodoroSessionNotifier() }
    single<SoundPlayer> { provideSoundPlayer() }
    single<CurrentTimeProvider> { SystemCurrentTimeProvider }
}
