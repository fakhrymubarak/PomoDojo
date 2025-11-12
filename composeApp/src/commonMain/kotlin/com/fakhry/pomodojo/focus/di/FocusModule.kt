package com.fakhry.pomodojo.focus.di

import com.fakhry.pomodojo.focus.data.db.PomoDojoRoomDatabase
import com.fakhry.pomodojo.focus.data.db.createDatabase
import com.fakhry.pomodojo.focus.data.repository.ActiveSessionRepositoryImpl
import com.fakhry.pomodojo.focus.data.repository.StaticQuoteRepository
import com.fakhry.pomodojo.focus.domain.provideFocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.repository.ActiveSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.usecase.SegmentCompletionSoundPlayer
import com.fakhry.pomodojo.focus.domain.usecase.SystemCurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.provideSegmentCompletionSoundPlayer
import com.fakhry.pomodojo.focus.ui.PomodoroSessionViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val focusModule = module {
    viewModelOf(::PomodoroSessionViewModel)
    single<ActiveSessionRepository> {
        ActiveSessionRepositoryImpl(get<PomoDojoRoomDatabase>())
    }
    singleOf(::StaticQuoteRepository) bind QuoteRepository::class
    singleOf(::CreatePomodoroSessionUseCase)
    single<PomoDojoRoomDatabase> { createDatabase() }
    single<FocusSessionNotifier> { provideFocusSessionNotifier() }
    single<SegmentCompletionSoundPlayer> { provideSegmentCompletionSoundPlayer() }
    single<CurrentTimeProvider> { SystemCurrentTimeProvider }
}
