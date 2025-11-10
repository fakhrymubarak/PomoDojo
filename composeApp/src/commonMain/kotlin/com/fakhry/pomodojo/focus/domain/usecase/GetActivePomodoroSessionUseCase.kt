package com.fakhry.pomodojo.focus.domain.usecase

import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

// TODO NEED TO ADJUST THE DATA LAYER.
class GetActivePomodoroSessionUseCase(
    private val quoteRepo: QuoteRepository,
    private val preferencesRepo: PreferencesRepository,
    private val pomodoroSessionRepo: PomodoroSessionRepository,
    private val dispatcher: DispatcherProvider,
) {
    suspend operator fun invoke() = withContext(dispatcher.io) {
        val activeSession = pomodoroSessionRepo.getActiveSession()
        val quoteDef = async { quoteRepo.getById(activeSession.quoteId) }
        val preferencesDef = async { preferencesRepo.preferences.first() }

        quoteDef.await()
        preferencesDef.await()

        return@withContext PomodoroSessionDomain()
    }
}
