package com.fakhry.pomodojo.features.focus.domain.usecase

import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.features.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.features.focus.domain.repository.ActiveSessionRepository
import com.fakhry.pomodojo.features.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.features.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.features.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.features.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.features.preferences.domain.usecase.InitPreferencesRepository
import com.fakhry.pomodojo.features.preferences.domain.usecase.PreferencesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CreatePomodoroSessionUseCase(
    private val sessionRepository: ActiveSessionRepository,
    private val quoteRepo: QuoteRepository,
    private val preferencesRepo: PreferencesRepository,
    private val initPreferencesRepository: InitPreferencesRepository,
    private val timelineBuilder: BuildTimerSegmentsUseCase,
    private val hourSplitter: BuildHourSplitTimelineUseCase,
    private val dispatcher: DispatcherProvider,
) {
    suspend operator fun invoke(now: Long) = withContext(dispatcher.io) {
        val quoteDef = async { quoteRepo.randomQuote() }
        val preferencesDef = async { preferencesRepo.preferences.first() }

        val quote = quoteDef.await()
        val preferences = preferencesDef.await()

        val activeSession = PomodoroSessionDomain(
            totalCycle = preferences.repeatCount,
            startedAtEpochMs = now,
            elapsedPauseEpochMs = 0L,
            timeline = TimelineDomain(
                segments = timelineBuilder(now, preferences),
                hourSplits = hourSplitter(preferences),
            ),
            quote = quote,
        )

        sessionRepository.saveActiveSession(activeSession)
        initPreferencesRepository.updateHasActiveSession(true)

        return@withContext activeSession
    }
}
