package com.fakhry.pomodojo.focus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.model.sessionId
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.usecase.SystemCurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import com.fakhry.pomodojo.utils.DispatcherProvider
import com.fakhry.pomodojo.utils.formatDurationMillis
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class PomodoroSessionViewModel(
    private val currentTimeProvider: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val createPomodoroSessionUseCase: CreatePomodoroSessionUseCase,
    private val sessionRepository: PomodoroSessionRepository,
    private val focusSessionNotifier: FocusSessionNotifier,
    private val dispatcher: DispatcherProvider,
) : ViewModel(), ContainerHost<PomodoroSessionUiState, PomodoroSessionSideEffect> {
    override val container =
        container<PomodoroSessionUiState, PomodoroSessionSideEffect>(PomodoroSessionUiState())

    private var timelineSegments: MutableList<TimelineSegmentUi> = mutableListOf()
    private var activeSegmentIndex: Int = 0
    private var tickerJob: Job? = null

    private val tickIntervalMillis = 1_000L

    init {
        restoreOrStartSession()
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
            TimerStatusDomain.Running -> {
                val refreshed = updateRunningSegment(active, now)
                if (refreshed.timerStatus == TimerStatusDomain.Completed) {
                    timelineSegments[activeSegmentIndex] = refreshed
                    reduce { state.withUpdatedTimeline(refreshed) }
                    return@intent
                }
                val paused = pauseSegment(refreshed, now)
                timelineSegments[activeSegmentIndex] = paused
                stopTicker()
                reduce { state.withUpdatedTimeline(paused) }
            }

            TimerStatusDomain.Paused -> {
                val resumed = resumeSegment(active, now)
                timelineSegments[activeSegmentIndex] = resumed
                reduce { state.withUpdatedTimeline(resumed) }
                startTicker()
            }

            else -> Unit
        }
        persistActiveSnapshotIfNeeded()
        updateNotification()
    }

    fun onDismissConfirmEnd() = intent {
        reduce { state.copy(isShowConfirmEndDialog = false) }
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(false))
    }

    fun onConfirmFinish() = intent {
        stopTicker()
        finalizeCurrentSegment()
        reduce {
            state.copy(
                isShowConfirmEndDialog = false,
                isComplete = true,
                activeSegment = timelineSegments.getOrNull(activeSegmentIndex)
                    ?: state.activeSegment,
                timeline = state.timeline.copy(segments = timelineSegments.toPersistentList()),
            )
        }
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(false))
        postSideEffect(PomodoroSessionSideEffect.OnSessionComplete)
        completeActiveSession()
    }

    private fun restoreOrStartSession() = intent {
        stopTicker()
        val now = currentTimeProvider.now()
        val hasStoredSession = sessionRepository.hasActiveSession()
        val session =
            if (hasStoredSession) {
                sessionRepository.getActiveSession()
            } else {
                createPomodoroSessionUseCase(now)
            }
        val prepared = prepareSession(session, now)
        reduce { prepared.uiState }
        if (prepared.uiState.isComplete) {
            stopTicker()
            completeActiveSession()
            postSideEffect(PomodoroSessionSideEffect.OnSessionComplete)
            return@intent
        }
        focusSessionNotifier.schedule(prepared.snapshot)
        if (prepared.didMutateTimeline || hasStoredSession) {
            sessionRepository.updateActiveSession(prepared.snapshot)
        }
        when (timelineSegments.getOrNull(activeSegmentIndex)?.timerStatus) {
            TimerStatusDomain.Running -> startTicker()
            else -> stopTicker()
        }
    }

    private fun prepareSession(
        session: PomodoroSessionDomain,
        now: Long,
    ): PreparedSession {
        timelineSegments =
            session.timeline.segments
                .map { it.toTimelineSegmentUi(now) }
                .toMutableList()
        activeSegmentIndex = timelineSegments.resolveActiveIndex()
        val mutated = fastForwardTimeline(now)
        activeSegmentIndex = timelineSegments.resolveActiveIndex()
        val refreshedSession =
            session.copy(
                timeline = TimelineDomain(
                    segments = timelineSegments.map { it.toDomainSegment() },
                    hourSplits = session.timeline.hourSplits,
                ),
            )
        val isComplete = timelineSegments.all { it.timerStatus == TimerStatusDomain.Completed }
        val uiState = refreshedSession.toUiState(timelineSegments, activeSegmentIndex, isComplete)
        return PreparedSession(
            uiState = uiState,
            snapshot = refreshedSession,
            didMutateTimeline = mutated,
        )
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        val active = timelineSegments.getOrNull(activeSegmentIndex) ?: return
        if (active.timerStatus != TimerStatusDomain.Running) return
        tickerJob = viewModelScope.launch(dispatcher.computation) {
            while (isActive) {
                delay(tickIntervalMillis)
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
            if (state.isComplete) return@intent
            val active = timelineSegments.getOrNull(activeSegmentIndex) ?: return@intent
            if (active.timerStatus != TimerStatusDomain.Running) return@intent

            val now = currentTimeProvider.now()
            var updatedSegment = updateRunningSegment(active, now)
            timelineSegments[activeSegmentIndex] = updatedSegment

            var advancedSegment = false
            while (updatedSegment.timerStatus == TimerStatusDomain.Completed) {
                advancedSegment = true
                if (!advanceToNextSegment(now)) {
                    reduce {
                        state.copy(
                            isComplete = true,
                            activeSegment = timelineSegments.getOrNull(activeSegmentIndex)
                                ?: state.activeSegment,
                            timeline = state.timeline.copy(
                                segments = timelineSegments.toPersistentList(),
                            ),
                        )
                    }
                    stopTicker()
                    completeActiveSession()
                    postSideEffect(PomodoroSessionSideEffect.OnSessionComplete)
                    return@intent
                }

                val newActive = timelineSegments[activeSegmentIndex]
                updatedSegment = updateRunningSegment(newActive, now)
                timelineSegments[activeSegmentIndex] = updatedSegment
            }

            reduce { state.withUpdatedTimeline(updatedSegment) }
            if (advancedSegment) {
                persistActiveSnapshotIfNeeded()
            }
            updateNotification()
        }
    }

    private fun advanceToNextSegment(referenceTime: Long): Boolean {
        if (activeSegmentIndex >= timelineSegments.lastIndex) {
            timelineSegments[activeSegmentIndex] =
                finalizeSegment(timelineSegments[activeSegmentIndex])
            return false
        }

        val currentSegment = timelineSegments[activeSegmentIndex]
        val nextStartAt = currentSegment.timer.finishedInMillis.takeIf { it > 0L } ?: referenceTime
        timelineSegments[activeSegmentIndex] = finalizeSegment(timelineSegments[activeSegmentIndex])
        activeSegmentIndex += 1
        timelineSegments[activeSegmentIndex] =
            prepareSegmentForRun(
                timelineSegments[activeSegmentIndex],
                startedAt = nextStartAt,
                referenceTime = referenceTime,
            )
        return true
    }

    private fun updateRunningSegment(segment: TimelineSegmentUi, now: Long): TimelineSegmentUi {
        val duration = segment.timer.durationEpochMs
        val remaining = (segment.timer.finishedInMillis - now).coerceAtLeast(0L)
        val progress = calculateTimerProgress(duration, remaining)
        val status = if (remaining == 0L) TimerStatusDomain.Completed else TimerStatusDomain.Running
        val timer = segment.timer.copy(
            progress = progress,
            formattedTime = remaining.formatDurationMillis(),
        )
        return segment.copy(timer = timer, timerStatus = status)
    }

    private fun pauseSegment(segment: TimelineSegmentUi, now: Long): TimelineSegmentUi {
        val timer = segment.timer.copy(startedPauseTime = now)
        return segment.copy(timer = timer, timerStatus = TimerStatusDomain.Paused)
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
        return segment.copy(timer = timer, timerStatus = TimerStatusDomain.Running)
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
        val status = if (remaining == 0L) TimerStatusDomain.Completed else TimerStatusDomain.Running
        return segment.copy(timer = timer, timerStatus = status)
    }

    private fun finalizeSegment(segment: TimelineSegmentUi): TimelineSegmentUi {
        val timer = segment.timer.copy(
            progress = 1f,
            formattedTime = 0L.formatDurationMillis(),
            startedPauseTime = 0L,
        )
        return segment.copy(timer = timer, timerStatus = TimerStatusDomain.Completed)
    }

    private fun finalizeCurrentSegment() {
        val current = timelineSegments.getOrNull(activeSegmentIndex) ?: return
        timelineSegments[activeSegmentIndex] = finalizeSegment(current)
    }

    private fun fastForwardTimeline(now: Long): Boolean {
        var mutated = false
        while (timelineSegments.isNotEmpty()) {
            val active = timelineSegments.getOrNull(activeSegmentIndex) ?: break
            when (active.timerStatus) {
                TimerStatusDomain.Running -> {
                    val refreshed = updateRunningSegment(active, now)
                    if (refreshed != active) {
                        timelineSegments[activeSegmentIndex] = refreshed
                        mutated = true
                    }
                    if (refreshed.timerStatus == TimerStatusDomain.Completed) {
                        if (!advanceToNextSegment(now)) {
                            return true
                        }
                        mutated = true
                        continue
                    }
                    break
                }

                TimerStatusDomain.Completed -> {
                    if (!advanceToNextSegment(now)) {
                        return true
                    }
                    mutated = true
                }

                TimerStatusDomain.Paused -> break

                TimerStatusDomain.Initial -> {
                    if (activeSegmentIndex == 0) {
                        timelineSegments[activeSegmentIndex] =
                            prepareSegmentForRun(active, now, now)
                        mutated = true
                    }
                    break
                }
            }
        }
        return mutated
    }

    private fun PomodoroSessionDomain.toUiState(
        segments: List<TimelineSegmentUi>,
        activeIndex: Int,
        isComplete: Boolean,
    ): PomodoroSessionUiState {
        val active = segments.getOrNull(activeIndex) ?: TimelineSegmentUi()
        return PomodoroSessionUiState(
            totalCycle = totalCycle,
            startedAtEpochMs = startedAtEpochMs,
            elapsedPauseEpochMs = elapsedPauseEpochMs,
            activeSegment = active,
            timeline = TimelineUiModel(
                segments = segments.toPersistentList(),
                hourSplits = timeline.hourSplits.toPersistentList(),
            ),
            quote = quote,
            isShowConfirmEndDialog = false,
            isComplete = isComplete,
        )
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

    private suspend fun persistActiveSnapshotIfNeeded() {
        val currentState = container.stateFlow.value
        if (currentState.isComplete) return
        buildSessionSnapshot(currentState)?.let { sessionRepository.updateActiveSession(it) }
    }

    private suspend fun completeActiveSession() {
        val currentState = container.stateFlow.value
        buildSessionSnapshot(currentState)?.let {
            sessionRepository.completeSession(it)
            focusSessionNotifier.cancel(it.sessionId())
        }
    }

    private fun buildSessionSnapshot(currentState: PomodoroSessionUiState): PomodoroSessionDomain? {
        if (timelineSegments.isEmpty()) return null
        return PomodoroSessionDomain(
            totalCycle = currentState.totalCycle,
            startedAtEpochMs = currentState.startedAtEpochMs,
            elapsedPauseEpochMs = currentState.elapsedPauseEpochMs,
            timeline =
            TimelineDomain(
                segments = timelineSegments.map { it.toDomainSegment() },
                hourSplits = currentState.timeline.hourSplits.toList(),
            ),
            quote = currentState.quote,
        )
    }

    private suspend fun updateNotification() {
        val currentState = container.stateFlow.value
        if (currentState.isComplete) {
            focusSessionNotifier.cancel(currentState.startedAtEpochMs.toString())
            return
        }
        buildSessionSnapshot(currentState)?.let { focusSessionNotifier.schedule(it) }
    }

    private data class PreparedSession(
        val uiState: PomodoroSessionUiState,
        val snapshot: PomodoroSessionDomain,
        val didMutateTimeline: Boolean,
    )

    override fun onCleared() {
        super.onCleared()
        stopTicker()
        runBlocking { persistActiveSnapshotIfNeeded() }
    }
}
