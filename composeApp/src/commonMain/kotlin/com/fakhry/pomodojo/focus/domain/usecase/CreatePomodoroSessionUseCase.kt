package com.fakhry.pomodojo.focus.domain.usecase

import com.fakhry.pomodojo.focus.domain.model.ActiveFocusSessionDomain
import com.fakhry.pomodojo.focus.domain.model.ActiveFocusSessionWithQuoteDomain
import com.fakhry.pomodojo.focus.domain.model.FocusTimerStatus
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CreatePomodoroSessionUseCase(
    private val quoteRepo: QuoteRepository,
    private val preferencesRepo: PreferencesRepository,
    private val pomodoroSessionRepo: PomodoroSessionRepository,
    private val dispatcher: DispatcherProvider,
) {

    suspend operator fun invoke() = withContext(dispatcher.io) {
        val quoteDef = async { quoteRepo.randomQuote() }
        val preferencesDef = async { preferencesRepo.preferences.first() }

        val quote = quoteDef.await()
        val preferences = preferencesDef.await()

        @OptIn(ExperimentalTime::class)
        val currentTime = Clock.System.now().toEpochMilliseconds()

        val activeSession = ActiveFocusSessionDomain(
            startedAtEpochMs = currentTime,
            elapsedPauseEpochMs = 0L,
            sessionStatus = FocusTimerStatus.RUNNING,
            repeatCount = preferences.repeatCount,
            focusMinutes = preferences.focusMinutes,
            breakMinutes = preferences.breakMinutes,
            longBreakEnabled = preferences.longBreakEnabled,
            longBreakAfter = preferences.longBreakAfter,
            longBreakMinutes = preferences.longBreakMinutes,
            quoteId = quote.id,
        )

        pomodoroSessionRepo.saveActiveSession(activeSession)

        return@withContext ActiveFocusSessionWithQuoteDomain(
            focusSession = activeSession,
            quote = quote,
            preferences = preferences,
        )
    }
}