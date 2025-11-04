package com.fakhry.pomodojo.focus.di

import com.fakhry.pomodojo.focus.data.db.PomoDojoRoomDatabase
import com.fakhry.pomodojo.focus.data.db.createDatabase
import com.fakhry.pomodojo.focus.data.repository.RoomFocusSessionRepository
import com.fakhry.pomodojo.focus.data.repository.StaticQuoteRepository
import com.fakhry.pomodojo.focus.domain.model.DefaultSessionIdGenerator
import com.fakhry.pomodojo.focus.domain.model.SessionIdGenerator
import com.fakhry.pomodojo.focus.domain.provideFocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.repository.FocusSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.usecase.SystemCurrentTimeProvider
import com.fakhry.pomodojo.focus.ui.FocusPomodoroViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val focusModule = module {
    viewModelOf(::FocusPomodoroViewModel)
    singleOf(::RoomFocusSessionRepository) bind FocusSessionRepository::class
    singleOf(::StaticQuoteRepository) bind QuoteRepository::class
    single<PomoDojoRoomDatabase> { createDatabase() }
    single<FocusSessionNotifier> { provideFocusSessionNotifier() }
    single<SessionIdGenerator> { DefaultSessionIdGenerator }
    single<CurrentTimeProvider> { SystemCurrentTimeProvider }
}
