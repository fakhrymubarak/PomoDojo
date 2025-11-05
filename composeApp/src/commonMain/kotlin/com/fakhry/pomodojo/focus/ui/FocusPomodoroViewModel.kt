package com.fakhry.pomodojo.focus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.focus.domain.model.FocusPhase
import com.fakhry.pomodojo.focus.domain.model.FocusSessionConfig
import com.fakhry.pomodojo.focus.domain.model.FocusSessionSnapshot
import com.fakhry.pomodojo.focus.domain.model.FocusTimerStatus
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.domain.model.SessionIdGenerator
import com.fakhry.pomodojo.focus.domain.repository.FocusSessionRepository
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.usecase.SystemCurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FocusPomodoroViewModel(
    private val sessionRepository: FocusSessionRepository,
    private val quoteRepository: QuoteRepository,
    private val sessionIdGenerator: SessionIdGenerator,
    private val currentTimeProvider: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val notifier: FocusSessionNotifier,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private var currentSnapshot: FocusSessionSnapshot? = null

    private val _state = kotlinx.coroutines.flow.MutableStateFlow<FocusPomodoroUiState>(FocusPomodoroUiState.Loading)
    val state: kotlinx.coroutines.flow.StateFlow<FocusPomodoroUiState> = _state

    init {
        viewModelScope.launch(dispatcherProvider.main) {
            sessionRepository.getActiveSession()?.let { restoreSnapshot(it) }
        }
    }

    suspend fun startNewSession(config: FocusSessionConfig) {
        val quote = quoteRepository.randomQuote() ?: QuoteContent(
            id = "quote-default",
            text = "Stay focused.",
            character = null,
            sourceTitle = null,
            metadata = null,
        )
        val totalSeconds = config.focusDurationMinutes * 60
        val currentTime = currentTimeProvider.now()
        val snapshot = FocusSessionSnapshot(
            sessionId = sessionIdGenerator.nextId(),
            status = FocusTimerStatus.RUNNING,
            focusDurationMinutes = config.focusDurationMinutes,
            shortBreakMinutes = config.shortBreakMinutes,
            longBreakMinutes = config.longBreakMinutes,
            autoStartNextPhase = config.autoStartNextPhase,
            autoStartBreaks = config.autoStartBreaks,
            phaseRemainingSeconds = totalSeconds,
            currentPhaseTotalSeconds = totalSeconds,
            completedCycles = 0,
            totalCycles = config.totalCycles,
            phase = FocusPhase.FOCUS,
            phaseStartedAtEpochMs = currentTime,
            quote = quote,
            startedAtEpochMs = currentTime,
            updatedAtEpochMs = currentTime,
        )
        sessionRepository.saveActiveSession(snapshot)
        currentSnapshot = snapshot
        notifier.schedule(snapshot)
        _state.value = snapshot.toActiveUiState()
    }

    suspend fun startFromPreferences(preferences: PreferencesDomain) {
        val config = FocusSessionConfig.fromPreferences(preferences)
        startNewSession(config)
    }

    fun togglePauseResume() = viewModelScope.launch{
        val snapshot = currentSnapshot ?: return@launch
        val now = currentTimeProvider.now()
        val newStatus = if (snapshot.status == FocusTimerStatus.RUNNING) {
            FocusTimerStatus.PAUSED
        } else {
            FocusTimerStatus.RUNNING
        }
        val adjustedSnapshot = snapshot.copy(
            status = newStatus,
            updatedAtEpochMs = now,
            phaseStartedAtEpochMs = if (newStatus == FocusTimerStatus.RUNNING) now else snapshot.phaseStartedAtEpochMs,
        )
        currentSnapshot = adjustedSnapshot
        viewModelScope.launch(dispatcherProvider.io) {
            sessionRepository.updateActiveSession(adjustedSnapshot)
        }
        when (newStatus) {
            FocusTimerStatus.PAUSED -> notifier.cancel(adjustedSnapshot.sessionId)
            FocusTimerStatus.RUNNING -> notifier.schedule(adjustedSnapshot)
        }
        (_state.value as? FocusPomodoroUiState.Active)?.let { active ->
            _state.value = active.copy(timerStatus = newStatus)
        }
    }

    fun onEndClicked() {
        _state.value = (_state.value as? FocusPomodoroUiState.Active)
            ?.copy(showConfirmEndDialog = true) ?: _state.value
    }

    fun onDismissConfirmEnd() {
        _state.value = (_state.value as? FocusPomodoroUiState.Active)
            ?.copy(showConfirmEndDialog = false) ?: _state.value
    }

    fun onConfirmFinish() {
        val snapshot = currentSnapshot ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            completeSnapshot(snapshot.copy(updatedAtEpochMs = currentTimeProvider.now(), phaseRemainingSeconds = 0))
        }
    }

    fun decrementTimer() {
        val snapshot = currentSnapshot ?: return
        if (snapshot.status != FocusTimerStatus.RUNNING) return

        val newRemaining = (snapshot.phaseRemainingSeconds - 1).coerceAtLeast(0)
        val now = currentTimeProvider.now()
        val updatedSnapshot = snapshot.copy(
            phaseRemainingSeconds = newRemaining,
            updatedAtEpochMs = now,
        )
        if (newRemaining <= 0) {
            viewModelScope.launch(dispatcherProvider.io) {
                completeSnapshot(updatedSnapshot)
            }
        } else {
            currentSnapshot = updatedSnapshot
            viewModelScope.launch(dispatcherProvider.io) {
                sessionRepository.updateActiveSession(updatedSnapshot)
            }
            (_state.value as? FocusPomodoroUiState.Active)?.let { active ->
                _state.value = active.copy(
                    remainingSeconds = newRemaining,
                    formattedTime = formatDuration(newRemaining),
                    progress = computeProgress(updatedSnapshot),
                )
            }
        }
    }

    private suspend fun completeSnapshot(snapshot: FocusSessionSnapshot) {
        notifier.cancel(snapshot.sessionId)
        sessionRepository.completeSession(snapshot)
        withContext(dispatcherProvider.main) {
            currentSnapshot = null
            _state.value = FocusPomodoroUiState.Completed
        }
    }

    private suspend fun restoreSnapshot(snapshot: FocusSessionSnapshot) {
        if (snapshot.status == FocusTimerStatus.RUNNING) {
            val now = currentTimeProvider.now()
            val elapsedSeconds = ((now - snapshot.updatedAtEpochMs) / 1000).toInt().coerceAtLeast(0)
            val remaining = snapshot.phaseRemainingSeconds - elapsedSeconds
            if (remaining <= 0) {
                viewModelScope.launch(dispatcherProvider.io) {
                    completeSnapshot(snapshot.copy(phaseRemainingSeconds = 0, updatedAtEpochMs = now))
                }
                return
            }
            val adjusted = snapshot.copy(
                phaseRemainingSeconds = remaining,
                updatedAtEpochMs = now,
            )
            currentSnapshot = adjusted
            viewModelScope.launch(dispatcherProvider.io) {
                sessionRepository.updateActiveSession(adjusted)
            }
            notifier.schedule(adjusted)
            _state.value = adjusted.toActiveUiState()
        } else {
            currentSnapshot = snapshot
            notifier.cancel(snapshot.sessionId)
            _state.value = snapshot.toActiveUiState()
        }
    }

    private fun formatDuration(remainingSeconds: Int): String {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        return "$minutes:$seconds"
    }

    private fun computeProgress(snapshot: FocusSessionSnapshot): Float {
        val total = snapshot.currentPhaseTotalSeconds.coerceAtLeast(1)
        return 1f - (snapshot.phaseRemainingSeconds.toFloat() / total.toFloat())
    }

    private fun FocusSessionSnapshot.toActiveUiState(
        showConfirmDialog: Boolean = false,
    ): FocusPomodoroUiState.Active {
        val safeQuote = quote ?: QuoteContent(
            id = "quote-missing",
            text = "You can do it!",
            character = null,
            sourceTitle = null,
            metadata = null,
        )
        return FocusPomodoroUiState.Active(
            timerStatus = status,
            remainingSeconds = phaseRemainingSeconds,
            totalSeconds = currentPhaseTotalSeconds.coerceAtLeast(1),
            formattedTime = formatDuration(phaseRemainingSeconds),
            progress = computeProgress(this).coerceIn(0f, 1f),
            completedSegments = completedCycles,
            totalSegments = totalCycles.coerceAtLeast(1),
            quote = safeQuote,
            phase = phase,
            showConfirmEndDialog = showConfirmDialog,
        )
    }
}
