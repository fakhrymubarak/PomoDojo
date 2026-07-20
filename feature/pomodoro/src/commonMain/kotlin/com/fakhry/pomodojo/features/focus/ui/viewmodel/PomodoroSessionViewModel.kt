package com.fakhry.pomodojo.features.focus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.core.designsystem.model.TimelineSegmentUi
import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.core.notification.PomodoroSessionNotifier
import com.fakhry.pomodojo.core.notification.SoundPlayer
import com.fakhry.pomodojo.core.utils.date.CurrentTimeProvider
import com.fakhry.pomodojo.core.utils.date.SystemCurrentTimeProvider
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.core.utils.primitives.formatDurationMillis
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.features.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.features.focus.ui.mapper.calculateTimerProgress
import com.fakhry.pomodojo.features.focus.ui.mapper.resolveActiveIndex
import com.fakhry.pomodojo.features.focus.ui.mapper.toCompletionSummary
import com.fakhry.pomodojo.features.focus.ui.mapper.toDomainSegment
import com.fakhry.pomodojo.features.focus.ui.mapper.toTimelineSegmentUi
import com.fakhry.pomodojo.features.focus.ui.mapper.toUiState
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionSideEffect
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

private const val TICK_INTERVAL_MILLIS = 1_000L
private const val TICK_UPDATE_NOTIF_INTERVAL_MILLIS = 5_000L

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

    private var timelineSegments: MutableList<TimelineSegmentUi> = mutableListOf()
    private var activeSegmentIndex: Int = 0
    private var tickerJob: Job? = null
    private var lastUpdatedNotif = 0L

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
        reduce { state.copy(isShowConfirmEndDialog = true) }
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(true))
    }

    fun togglePauseResume() = intent {
        val active = timelineSegments.getOrNull(activeSegmentIndex) ?: return@intent
        val now = currentTimeProvider.now()
        when (active.timerStatus) {
            TimerStatusUi.RUNNING -> {
                val refreshed = updateRunningSegment(active, now)
                if (refreshed.timerStatus == TimerStatusUi.COMPLETED) {
                    timelineSegments[activeSegmentIndex] = refreshed
                    reduce { state.withUpdatedTimeline(refreshed) }
                    return@intent
                }
                val paused = pauseSegment(refreshed, now)
                timelineSegments[activeSegmentIndex] = paused
                stopTicker()
                reduce { state.withUpdatedTimeline(paused) }
            }

            TimerStatusUi.PAUSED -> {
                val resumed = resumeSegment(active, now)
                timelineSegments[activeSegmentIndex] = resumed
                reduce { state.withUpdatedTimeline(resumed) }
                startTicker()
            }

            else -> Unit
        }
        persistActiveSnapshotIfNeeded()
        updateNotification(forceUpdate = true)
    }

    fun onDismissConfirmEnd() = intent {
        reduce { state.copy(isShowConfirmEndDialog = false) }
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(false))
    }

    fun onConfirmFinish() = intent {
        stopTicker()
        finalizeCurrentSegment()
        reduce {
            state.withGateClosed().copy(
                isShowConfirmEndDialog = false,
                isComplete = true,
                activeSegment = timelineSegments.getOrNull(
                    activeSegmentIndex,
                ) ?: state.activeSegment,
                timeline = state.timeline.copy(segments = timelineSegments.toPersistentList()),
            )
        }
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(false))
        val completionSummary = state.toCompletionSummary()
        postSideEffect(PomodoroSessionSideEffect.OnSessionComplete(completionSummary))
        completeActiveSession()
    }

    private fun restoreOrStartSession() = intent {
        viewModelScope.launch(dispatcher.io) {
            stopTicker()
            val now = currentTimeProvider.now()
            val hasStoredSession = repoGroup.sessionRepository.hasActiveSession()
            val session = if (hasStoredSession) {
                repoGroup.sessionRepository.getActiveSession()
            } else {
                createPomodoroSessionUseCase(now)
            }
            val prepared = prepareSession(session, now)

            reduce { prepared.uiState }
            resetNotificationThrottle()
            updateNotification()

            if (prepared.uiState.isComplete) {
                stopTicker()
                completeActiveSession()
                val completionSummary = state.toCompletionSummary()
                postSideEffect(PomodoroSessionSideEffect.OnSessionComplete(completionSummary))
                return@launch
            }
            if (prepared.didMutateTimeline || hasStoredSession) {
                repoGroup.sessionRepository.saveActiveSession(prepared.snapshot)
            }
            when (timelineSegments.getOrNull(activeSegmentIndex)?.timerStatus) {
                TimerStatusUi.RUNNING -> startTicker()
                else -> stopTicker()
            }
        }
    }

    private fun prepareSession(session: PomodoroSessionDomain, now: Long): PreparedSession {
        timelineSegments = session.timeline.segments.map {
            it.toTimelineSegmentUi(now)
        }.toMutableList()
        activeSegmentIndex = timelineSegments.resolveActiveIndex()
        val mutated = fastForwardTimeline(now)
        activeSegmentIndex = timelineSegments.resolveActiveIndex()

        // A phase that completed while the app was away leaves an open gate: point the
        // active index at the finished segment so restore replays its animation + dialog.
        val gateNextIndex = timelineSegments.awaitingGateNextIndex()
        if (gateNextIndex != null) {
            activeSegmentIndex = gateNextIndex - 1
        }

        val refreshedSession = session.copy(
            timeline = TimelineDomain(
                segments = timelineSegments.map { it.toDomainSegment() },
                hourSplits = session.timeline.hourSplits,
            ),
        )
        val isComplete = timelineSegments.all { it.timerStatus == TimerStatusUi.COMPLETED }
        val uiState = refreshedSession.toUiState(timelineSegments, activeSegmentIndex, isComplete)
            .let {
                if (gateNextIndex !=
                    null
                ) {
                    it.withGateOpen(timelineSegments[activeSegmentIndex])
                } else {
                    it
                }
            }
        return PreparedSession(
            uiState = uiState,
            snapshot = refreshedSession,
            didMutateTimeline = mutated,
        )
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        val active = timelineSegments.getOrNull(activeSegmentIndex) ?: return
        if (active.timerStatus != TimerStatusUi.RUNNING) return
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
        intent {
            if (state.isComplete || state.awaitingContinue) return@intent
            val active = timelineSegments.getOrNull(activeSegmentIndex) ?: return@intent
            if (active.timerStatus != TimerStatusUi.RUNNING) return@intent

            val now = currentTimeProvider.now()
            val updatedSegment = updateRunningSegment(active, now)
            timelineSegments[activeSegmentIndex] = updatedSegment

            if (updatedSegment.timerStatus != TimerStatusUi.COMPLETED) {
                reduce { state.withUpdatedTimeline(updatedSegment) }
                return@intent
            }

            // Phase finished: stop the ticker and signal completion once.
            soundPlayer.playSegmentCompleted()
            stopTicker()

            if (activeSegmentIndex >= timelineSegments.lastIndex) {
                // Final segment of the session keeps the existing completion flow.
                timelineSegments[activeSegmentIndex] = finalizeSegment(updatedSegment)
                reduce {
                    state.copy(
                        isComplete = true,
                        activeSegment = timelineSegments[activeSegmentIndex],
                        timeline = state.timeline.copy(
                            segments = timelineSegments.toPersistentList(),
                        ),
                    )
                }
                completeActiveSession()
                val completionSummary = state.toCompletionSummary()
                postSideEffect(PomodoroSessionSideEffect.OnSessionComplete(completionSummary))
                return@intent
            }

            // Mid-session transition: park at the gate (animation + continue/finish dialog).
            // The next phase stays INITIAL until the user taps continue.
            reduce { state.withGateOpen(updatedSegment) }
            persistActiveSnapshotIfNeeded()
            updateNotification(forceUpdate = true)
        }
    }

    fun onContinueNextPhase() = intent {
        if (!state.awaitingContinue) return@intent
        if (activeSegmentIndex >= timelineSegments.lastIndex) return@intent

        // Start the next phase fresh from the tap time (idle time at the gate is free).
        val now = currentTimeProvider.now()
        timelineSegments[activeSegmentIndex] = finalizeSegment(timelineSegments[activeSegmentIndex])
        activeSegmentIndex += 1
        timelineSegments[activeSegmentIndex] = prepareSegmentForRun(
            timelineSegments[activeSegmentIndex],
            startedAt = now,
            referenceTime = now,
        )
        reduce { state.withGateClosed().withUpdatedTimeline() }
        persistActiveSnapshotIfNeeded()
        startTicker()
        updateNotification(forceUpdate = true)
    }

    private fun updateRunningSegment(segment: TimelineSegmentUi, now: Long): TimelineSegmentUi {
        val duration = segment.timer.durationEpochMs
        val remaining = (segment.timer.finishedInMillis - now).coerceAtLeast(0L)
        val progress = calculateTimerProgress(duration, remaining)
        val status = if (remaining == 0L) TimerStatusUi.COMPLETED else TimerStatusUi.RUNNING
        val timer = segment.timer.copy(
            progress = progress,
            formattedTime = remaining.formatDurationMillis(),
        )
        return segment.copy(timer = timer, timerStatus = status)
    }

    private fun pauseSegment(segment: TimelineSegmentUi, now: Long): TimelineSegmentUi {
        val timer = segment.timer.copy(startedPauseTime = now)
        return segment.copy(timer = timer, timerStatus = TimerStatusUi.PAUSED)
    }

    private fun resumeSegment(segment: TimelineSegmentUi, now: Long): TimelineSegmentUi {
        val pauseStartedAt = segment.timer.startedPauseTime.takeIf { it > 0L } ?: now
        val pausedDuration = (now - pauseStartedAt).coerceAtLeast(0L)
        val newFinishTime = segment.timer.finishedInMillis + pausedDuration
        val remaining = (newFinishTime - now).coerceAtLeast(0L)
        val timer = segment.timer.copy(
            startedPauseTime = 0L,
            elapsedPauseTime = segment.timer.elapsedPauseTime + pausedDuration,
            finishedInMillis = newFinishTime,
            formattedTime = remaining.formatDurationMillis(),
            progress = calculateTimerProgress(segment.timer.durationEpochMs, remaining),
        )
        return segment.copy(timer = timer, timerStatus = TimerStatusUi.RUNNING)
    }

    private fun prepareSegmentForRun(
        segment: TimelineSegmentUi,
        startedAt: Long,
        referenceTime: Long,
    ): TimelineSegmentUi {
        val duration = segment.timer.durationEpochMs
        val start = startedAt.takeIf { it > 0L } ?: referenceTime
        val finishedAt = start + duration
        val remaining = (finishedAt - referenceTime).coerceAtLeast(0L)
        val timer = segment.timer.copy(
            progress = calculateTimerProgress(duration, remaining),
            finishedInMillis = finishedAt,
            formattedTime = remaining.formatDurationMillis(),
            startedPauseTime = 0L,
            elapsedPauseTime = 0L,
        )
        val status = if (remaining == 0L) TimerStatusUi.COMPLETED else TimerStatusUi.RUNNING
        return segment.copy(timer = timer, timerStatus = status)
    }

    private fun finalizeSegment(segment: TimelineSegmentUi): TimelineSegmentUi {
        val timer = segment.timer.copy(
            progress = segment.timer.progress.coerceAtMost(1f),
            formattedTime = segment.timer.formattedTime,
            startedPauseTime = 0L,
        )
        return segment.copy(timer = timer, timerStatus = TimerStatusUi.COMPLETED)
    }

    private fun finalizeCurrentSegment() {
        val current = timelineSegments.getOrNull(activeSegmentIndex) ?: return
        timelineSegments[activeSegmentIndex] = finalizeSegment(current)
    }

    private fun fastForwardTimeline(now: Long): Boolean {
        val active = timelineSegments.getOrNull(activeSegmentIndex) ?: return false
        return when (active.timerStatus) {
            // Refresh the running segment. If it elapsed while the app was away it
            // becomes COMPLETED and stays parked here — the gate is honored on restore
            // instead of auto-advancing to the next phase.
            TimerStatusUi.RUNNING -> {
                val refreshed = updateRunningSegment(active, now)
                if (refreshed != active) {
                    timelineSegments[activeSegmentIndex] = refreshed
                    true
                } else {
                    false
                }
            }

            // A brand-new session auto-starts its first phase; a later INITIAL segment
            // is a parked gate awaiting the user's continue and must be left untouched.
            TimerStatusUi.INITIAL -> {
                if (activeSegmentIndex == 0) {
                    timelineSegments[activeSegmentIndex] = prepareSegmentForRun(active, now, now)
                    true
                } else {
                    false
                }
            }

            TimerStatusUi.COMPLETED, TimerStatusUi.PAUSED -> false
        }
    }

    /** Opens the phase-transition gate: park on [finishedSegment], expose the next phase. */
    private fun PomodoroSessionUiState.withGateOpen(
        finishedSegment: TimelineSegmentUi,
    ): PomodoroSessionUiState {
        val next = timelineSegments.getOrNull(activeSegmentIndex + 1)
        return copy(
            activeSegment = finishedSegment,
            timeline = timeline.copy(segments = timelineSegments.toPersistentList()),
            awaitingContinue = true,
            finishedPhaseType = finishedSegment.type,
            nextPhaseType = next?.type,
            nextPhaseDurationMs = next?.timer?.durationEpochMs ?: 0L,
        )
    }

    private fun PomodoroSessionUiState.withGateClosed(): PomodoroSessionUiState = copy(
        awaitingContinue = false,
        finishedPhaseType = null,
        nextPhaseType = null,
        nextPhaseDurationMs = 0L,
    )

    /** Index of the pending phase waiting behind an open gate, or null when none. */
    private fun List<TimelineSegmentUi>.awaitingGateNextIndex(): Int? {
        val firstPending = indexOfFirst { it.timerStatus != TimerStatusUi.COMPLETED }
        if (firstPending <= 0) return null
        return firstPending.takeIf { this[it].timerStatus == TimerStatusUi.INITIAL }
    }

    private fun PomodoroSessionUiState.withUpdatedTimeline(
        activeOverride: TimelineSegmentUi? = null,
    ): PomodoroSessionUiState {
        val segments = this@PomodoroSessionViewModel.timelineSegments
        val index = this@PomodoroSessionViewModel.activeSegmentIndex
        val newActive = activeOverride ?: segments.getOrNull(index) ?: activeSegment
        return copy(
            activeSegment = newActive,
            timeline = timeline.copy(segments = segments.toPersistentList()),
        )
    }

    private fun persistActiveSnapshotIfNeeded() = viewModelScope.launch(dispatcher.io) {
        val currentState = container.stateFlow.value
        if (currentState.isComplete) return@launch
        buildSessionSnapshot(currentState)?.let {
            repoGroup.sessionRepository.saveActiveSession(it)
        }
    }

    private suspend fun completeActiveSession() {
        val currentState = container.stateFlow.value
        buildSessionSnapshot(currentState)?.let {
            repoGroup.sessionRepository.clearActiveSession()
            repoGroup.historyRepository.insertHistory(it)
            pomodoroSessionNotifier.cancel(it.sessionId())
        }
        resetNotificationThrottle()
    }

    private fun buildSessionSnapshot(currentState: PomodoroSessionUiState): PomodoroSessionDomain? {
        if (timelineSegments.isEmpty()) return null
        return PomodoroSessionDomain(
            totalCycle = currentState.totalCycle,
            startedAtEpochMs = currentState.startedAtEpochMs,
            elapsedPauseEpochMs = currentState.elapsedPauseEpochMs,
            timeline = TimelineDomain(
                segments = timelineSegments.map { it.toDomainSegment() },
                hourSplits = currentState.timeline.hourSplits.toList(),
            ),
            quote = currentState.quote,
        )
    }

    private fun updateNotification(forceUpdate: Boolean = false) =
        viewModelScope.launch(dispatcher.main) {
            val currentState = container.stateFlow.value
            if (currentState.isComplete) {
                pomodoroSessionNotifier.cancel(currentState.startedAtEpochMs.toString())
                resetNotificationThrottle()
                return@launch
            }

            // Update notification after
            val now = currentTimeProvider.now()
            val shouldThrottle =
                !forceUpdate && now - lastUpdatedNotif <= TICK_UPDATE_NOTIF_INTERVAL_MILLIS
            if (shouldThrottle) return@launch
            buildSessionSnapshot(currentState)?.let { pomodoroSessionNotifier.schedule(it) }
            lastUpdatedNotif = now
        }

    private fun resetNotificationThrottle() {
        lastUpdatedNotif = 0L
    }

    override fun onCleared() {
        persistActiveSnapshotIfNeeded()
        stopTicker()
        super.onCleared()
    }
}
