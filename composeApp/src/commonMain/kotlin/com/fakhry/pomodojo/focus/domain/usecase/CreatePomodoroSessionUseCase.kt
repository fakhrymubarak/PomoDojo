package com.fakhry.pomodojo.focus.domain.usecase

import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildFocusTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CreatePomodoroSessionUseCase(
    private val quoteRepo: QuoteRepository,
    private val preferencesRepo: PreferencesRepository,
    private val pomodoroSessionRepo: PomodoroSessionRepository,
    private val timelineBuilder: BuildFocusTimelineUseCase,
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

//        pomodoroSessionRepo.saveActiveSession(activeSession)

        return@withContext activeSession
    }
}
