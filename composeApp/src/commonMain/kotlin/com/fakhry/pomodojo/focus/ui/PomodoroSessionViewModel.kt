package com.fakhry.pomodojo.focus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
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
        val session =
            if (sessionRepository.hasActiveSession()) {
                sessionRepository.getActiveSession()
            } else {
                createPomodoroSessionUseCase(now)
            }
        val uiState = prepareSessionUi(session, now)
        reduce { uiState }
        when (timelineSegments.getOrNull(activeSegmentIndex)?.timerStatus) {
            TimerStatusDomain.Running -> startTicker()
            else -> stopTicker()
        }
    }

    private fun prepareSessionUi(session: PomodoroSessionDomain, now: Long): PomodoroSessionUiState {
        timelineSegments =
            session.timeline.segments
                .map { it.toTimelineSegmentUi(now) }
                .toMutableList()
        activeSegmentIndex = timelineSegments.resolveActiveIndex()
        return session.toUiState(timelineSegments, activeSegmentIndex)
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
        }
    }

    private fun advanceToNextSegment(now: Long): Boolean {
        if (activeSegmentIndex >= timelineSegments.lastIndex) {
            timelineSegments[activeSegmentIndex] =
                finalizeSegment(timelineSegments[activeSegmentIndex])
            return false
        }

        timelineSegments[activeSegmentIndex] = finalizeSegment(timelineSegments[activeSegmentIndex])
        activeSegmentIndex += 1
        timelineSegments[activeSegmentIndex] =
            prepareSegmentForRun(timelineSegments[activeSegmentIndex], now)
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

    private fun prepareSegmentForRun(segment: TimelineSegmentUi, now: Long): TimelineSegmentUi {
        val duration = segment.timer.durationEpochMs
        val timer = segment.timer.copy(
            progress = 0f,
            finishedInMillis = now + duration,
            formattedTime = duration.formatDurationMillis(),
            startedPauseTime = 0L,
            elapsedPauseTime = 0L,
        )
        return segment.copy(timer = timer, timerStatus = TimerStatusDomain.Running)
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

    private fun PomodoroSessionDomain.toUiState(
        segments: List<TimelineSegmentUi>,
        activeIndex: Int,
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
            isComplete = false,
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
        buildSessionSnapshot(currentState)?.let { sessionRepository.completeSession(it) }
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

    override fun onCleared() {
        super.onCleared()
        stopTicker()
        runBlocking { persistActiveSnapshotIfNeeded() }
    }
}
