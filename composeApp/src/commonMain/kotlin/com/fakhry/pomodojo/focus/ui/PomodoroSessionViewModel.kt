package com.fakhry.pomodojo.focus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.SystemCurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import com.fakhry.pomodojo.preferences.ui.model.TimerUi
import com.fakhry.pomodojo.utils.DispatcherProvider
import com.fakhry.pomodojo.utils.formatDurationMillis
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class PomodoroSessionViewModel(
    private val currentTimeProvider: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val createPomodoroSessionUseCase: CreatePomodoroSessionUseCase,
    private val dispatcher: DispatcherProvider,
) : ViewModel(), ContainerHost<PomodoroSessionUiState, PomodoroSessionSideEffect> {
    override val container =
        container<PomodoroSessionUiState, PomodoroSessionSideEffect>(PomodoroSessionUiState())

    private var timelineSegments: MutableList<TimelineSegmentUi> = mutableListOf()
    private var activeSegmentIndex: Int = 0
    private var tickerJob: Job? = null

    private val tickIntervalMillis = 1_000L

    init {
        startNewSession()
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
    }

    private fun startNewSession() = intent {
        stopTicker()
        val now = currentTimeProvider.now()
        val session = createPomodoroSessionUseCase(now)

        timelineSegments = session.timeline.segments.mapIndexed { index, segment ->
            buildTimelineSegment(segment, isRunning = index == 0, now = now)
        }.toMutableList()
        activeSegmentIndex =
            timelineSegments.indexOfFirst { it.timerStatus == TimerStatusDomain.Running }
                .takeUnless { it < 0 } ?: 0

        val uiState = session.toUiState(timelineSegments, activeSegmentIndex)
        reduce { uiState }

        if (timelineSegments.isNotEmpty()) {
            startTicker()
        }
    }

    private fun startTicker() {
        if (timelineSegments.isEmpty()) return
        if (tickerJob?.isActive == true) return
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

            while (updatedSegment.timerStatus == TimerStatusDomain.Completed) {
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
                    postSideEffect(PomodoroSessionSideEffect.OnSessionComplete)
                    return@intent
                }

                val newActive = timelineSegments[activeSegmentIndex]
                updatedSegment = updateRunningSegment(newActive, now)
                timelineSegments[activeSegmentIndex] = updatedSegment
            }

            reduce { state.withUpdatedTimeline(updatedSegment) }
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
        val progress = calculateProgress(duration, remaining)
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
            progress = calculateProgress(segment.timer.durationEpochMs, remaining),
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

    private fun buildTimelineSegment(
        segment: TimerSegmentsDomain,
        isRunning: Boolean,
        now: Long,
    ): TimelineSegmentUi {
        val duration = segment.timer.durationEpochMs
        val finishedAt = if (isRunning) now + duration else 0L
        val timer = TimerUi(
            progress = 0f,
            durationEpochMs = duration,
            finishedInMillis = finishedAt,
            formattedTime = duration.formatDurationMillis(),
            startedPauseTime = 0L,
            elapsedPauseTime = 0L,
        )
        return TimelineSegmentUi(
            type = segment.type,
            cycleNumber = segment.cycleNumber,
            timer = timer,
            timerStatus = if (isRunning) TimerStatusDomain.Running else TimerStatusDomain.Initial,
        )
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

    private fun calculateProgress(duration: Long, remaining: Long): Float {
        if (duration <= 0L) return 1f
        val completed = duration - remaining.coerceAtMost(duration)
        return (completed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
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

    override fun onCleared() {
        super.onCleared()
        stopTicker()
    }
}
