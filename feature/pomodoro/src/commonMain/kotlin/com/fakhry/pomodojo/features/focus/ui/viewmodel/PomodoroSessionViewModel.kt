package com.fakhry.pomodojo.features.focus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.core.notification.PomodoroSessionNotifier
import com.fakhry.pomodojo.core.notification.SoundPlayer
import com.fakhry.pomodojo.core.utils.date.CurrentTimeProvider
import com.fakhry.pomodojo.core.utils.date.SystemCurrentTimeProvider
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelinePhase
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.completionSummary
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.continuedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.elapsedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.finishedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.pausedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.phaseAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.resumedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.skippedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.started
import com.fakhry.pomodojo.features.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.features.focus.ui.mapper.toUiState
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionSideEffect
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import com.fakhry.pomodojo.features.focus.ui.model.SessionConfirm
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container

private const val TICK_INTERVAL_MILLIS = 1_000L

private typealias SessionSyntax = Syntax<PomodoroSessionUiState, PomodoroSessionSideEffect>

class PomodoroSessionViewModel(
    private val currentTimeProvider: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val createPomodoroSessionUseCase: CreatePomodoroSessionUseCase,
    private val repoGroup: PomodoroSessionRepoGroup,
    private val soundPlayer: SoundPlayer,
    private val pomodoroSessionNotifier: PomodoroSessionNotifier,
    private val dispatcher: DispatcherProvider,
) : ViewModel(), ContainerHost<PomodoroSessionUiState, PomodoroSessionSideEffect> {
    override val container =
        container<PomodoroSessionUiState, PomodoroSessionSideEffect>(PomodoroSessionUiState())

    private val _alwaysOnDisplay = MutableStateFlow(true)
    val alwaysOnDisplay = _alwaysOnDisplay.asStateFlow()

    /** The Session being run; the Orbit state is its projection. Null until it is loaded. */
    private var session: PomodoroSessionDomain? = null
    private var tickerJob: Job? = null

    init {
        getAlwaysOnDisplayState()
        restoreOrStartSession()
    }

    private fun getAlwaysOnDisplayState() = viewModelScope.launch(dispatcher.io) {
        repoGroup.preferencesRepository.preferences.collect { preferences ->
            _alwaysOnDisplay.update { preferences.alwaysOnDisplayEnabled }
        }
    }

    fun onEndClicked() = intent {
        if (state.isComplete) return@intent
        reduce { state.copy(pendingConfirm = SessionConfirm.END_SESSION) }
    }

    fun onSkipClicked() = intent {
        if (state.isComplete) return@intent
        reduce { state.copy(pendingConfirm = SessionConfirm.SKIP_BREAK) }
    }

    fun onDismissConfirm() = intent {
        reduce { state.copy(pendingConfirm = null) }
    }

    /** Back answers an open confirmation with "no"; otherwise it asks to end the Session. */
    fun onBackPressed() = intent {
        when {
            state.pendingConfirm != null -> reduce { state.copy(pendingConfirm = null) }
            !state.isComplete -> reduce { state.copy(pendingConfirm = SessionConfirm.END_SESSION) }
        }
    }

    fun togglePauseResume() = intent {
        transition { now ->
            when (phaseAt(now)) {
                is TimelinePhase.Running -> pausedAt(now)
                is TimelinePhase.Paused -> resumedAt(now)
                else -> this
            }
        }
    }

    fun onConfirmSkip() = intent {
        reduce { state.copy(pendingConfirm = null) }
        transition { skippedAt(it) }
    }

    fun onConfirmFinish() = intent {
        if (state.isComplete) return@intent
        reduce { state.copy(pendingConfirm = null) }
        transition(finish = true) { finishedAt(it) }
    }

    fun onContinueNextPhase() = intent {
        transition { continuedAt(it) }
    }

    private fun restoreOrStartSession() = intent {
        viewModelScope.launch(dispatcher.io) {
            stopTicker()
            val now = currentTimeProvider.now()
            val store = repoGroup.sessionStore
            val created = if (store.session.first() ==
                null
            ) {
                createPomodoroSessionUseCase(now)
            } else {
                null
            }
            // A Timeline plan starts now; a Segment that ran out while away parks at the Gate,
            // so restore replays the transition.
            val restored = store.update { stored ->
                (stored ?: created)?.let {
                    it.copy(timeline = it.timeline.started(now).elapsedAt(now))
                }
            } ?: return@launch
            session = restored
            if (restored.timeline.phaseAt(now) == TimelinePhase.Complete) {
                store.take()?.let { completeSession(it, now) }
                return@launch
            }
            reduce { state.rendered(restored, now) }
            scheduleNotification(restored)
            syncTicker(restored, now)
        }
    }

    /**
     * Applies [change] at the current time to the stored Session, which the segment alarm may have
     * moved on in the meantime. A Segment that ran out is completed first (with the chime).
     * Reaching the end of the Timeline, or [finish], completes the Session.
     */
    private suspend fun SessionSyntax.transition(
        finish: Boolean = false,
        change: TimelineDomain.(now: Long) -> TimelineDomain,
    ) {
        val local = session?.takeUnless { state.isComplete } ?: return
        val now = currentTimeProvider.now()
        val applied = { stored: PomodoroSessionDomain ->
            stored.copy(timeline = stored.timeline.elapsedAt(now).change(now))
        }
        if (!finish && applied(local) == local) {
            // A plain tick: only the clock moved, so there is nothing to store.
            reduce { state.rendered(local, now) }
            return
        }
        if (local.timeline.elapsedAt(now) != local.timeline) soundPlayer.playSegmentCompleted()

        val updated = repoGroup.sessionStore.update { it?.let(applied) }
        session = updated
        when {
            updated == null -> stopTicker()

            finish || updated.timeline.phaseAt(now) == TimelinePhase.Complete ->
                repoGroup.sessionStore.take()?.let { completeSession(it, now) }

            else -> {
                reduce { state.rendered(updated, now) }
                syncTicker(updated, now)
                scheduleNotification(updated)
            }
        }
    }

    /** Only the caller that took the Session gets here, so History is written at most once. */
    private suspend fun SessionSyntax.completeSession(completed: PomodoroSessionDomain, now: Long) {
        stopTicker()
        reduce { state.rendered(completed, now, isComplete = true) }
        repoGroup.historyRepository.insertHistory(completed)
        pomodoroSessionNotifier.cancel(completed.sessionId())
        val summary = completed.timeline.completionSummary().toUiState()
        postSideEffect(PomodoroSessionSideEffect.OnSessionComplete(summary))
    }

    /**
     * Keeps the pending confirmation, which is not part of the Session. The Gate replaces a
     * pending Skip (there is nothing left to skip); a pending End still applies there.
     */
    private fun PomodoroSessionUiState.rendered(
        session: PomodoroSessionDomain,
        now: Long,
        isComplete: Boolean = this.isComplete,
    ): PomodoroSessionUiState {
        val next = session.toUiState(now, isComplete)
        val stale = next.isComplete ||
            (next.awaitingContinue && pendingConfirm == SessionConfirm.SKIP_BREAK)
        return next.copy(pendingConfirm = pendingConfirm.takeUnless { stale })
    }

    private fun syncTicker(session: PomodoroSessionDomain, now: Long) {
        if (session.timeline.phaseAt(now) is TimelinePhase.Running) startTicker() else stopTicker()
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch(dispatcher.computation) {
            while (isActive) {
                delay(TICK_INTERVAL_MILLIS)
                handleTick()
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun handleTick() {
        intent { transition { this } }
    }

    private fun scheduleNotification(session: PomodoroSessionDomain) =
        viewModelScope.launch(dispatcher.main) {
            pomodoroSessionNotifier.schedule(session)
        }

    override fun onCleared() {
        stopTicker()
        super.onCleared()
    }
}
