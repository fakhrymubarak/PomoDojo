package com.fakhry.pomodojo.focus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.GetActivePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.SystemCurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.ui.model.TimelineSegmentUi
import com.fakhry.pomodojo.utils.DispatcherProvider
import com.fakhry.pomodojo.utils.formatDurationMillis
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TIMER_TICK_MILLIS = 5_000L

class FocusPomodoroViewModel(
    private val pomodoroSessionRepo: PomodoroSessionRepository,
    private val currentTimeProvider: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val createPomodoroSessionUseCase: CreatePomodoroSessionUseCase,
    private val getPomodoroSessionUseCase: GetActivePomodoroSessionUseCase,
    private val dispatcher: DispatcherProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(PomodoroSessionUiState())
    val state = _state.asStateFlow()

    private var tickerJob: Job? = null

    init {
        startPomodoroSession()
    }

    private fun startPomodoroSession() = viewModelScope.launch(dispatcher.io) {
        val now = currentTimeProvider.now()
        cancelTicker()
        val session = if (pomodoroSessionRepo.hasActiveSession()) {
            getPomodoroSessionUseCase().toPomodoroUiSessionUi()
        } else {
            createPomodoroSessionUseCase(now).toPomodoroUiSessionUi()
        }

        _state.update { session }
        startTicker()
    }

    fun togglePauseResume() = viewModelScope.launch(dispatcher.io) {
    }

    fun onEndClicked() {
        _state.update { it.copy(isShowConfirmEndDialog = true) }
    }

    fun onDismissConfirmEnd() {
        _state.update { it.copy(isShowConfirmEndDialog = false) }
    }

    fun onConfirmFinish() {
        _state.update { it.copy(isShowConfirmEndDialog = false, isComplete = true) }
        viewModelScope.launch(dispatcher.io) {
//            activeSession?.let { completeSession(it) }
        }
    }

    // TODO :
    // - implement repository complete session
    private suspend fun completeSession() {
        _state.update {
            it.copy(isComplete = true)
        }
    }


    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch(dispatcher.computation) {
            while (isActive) {
                delay(5L)
                handleTimerTick()
            }
        }
    }

    private fun cancelTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    // Update timeline
    // Update formattedTime
    private fun handleTimerTick() {
        _state.update { pomodoroUi ->
            val activeSegment = pomodoroUi.activeSegment
            val timeline = pomodoroUi.timeline
            val timelineSegments = timeline.segments.toMutableList()
            val activeSegmentIndex = timelineSegments.indexOf(activeSegment)

            val updatedTimerStatus = when (val timerStatus = activeSegment.timerStatus) {
                is TimerStatusDomain.Running -> {
                    val remainingMillis = timerStatus.remainingMillis - TIMER_TICK_MILLIS
                    if (remainingMillis <= 0) {
                        TimerStatusDomain.Completed()
                    } else {
                        activeSegment.timerStatus.copy(
                            progress = 1 - (remainingMillis.toFloat() / timerStatus.durationEpochMs),
                            remainingMillis = remainingMillis,
                            formattedTime = remainingMillis.formatDurationMillis(),
                        )
                    }

                }
                is TimerStatusDomain.Paused -> {
                    activeSegment.timerStatus.copy(
                        progress = 1 - (timerStatus.remainingMillis.toFloat() / timerStatus.durationEpochMs),
                        remainingMillis = timerStatus.remainingMillis,
                        formattedTime = timerStatus.remainingMillis.formatDurationMillis(),
                    )
                }
                else -> activeSegment.timerStatus
            }

            // Update active segments
            val newActiveSegment = activeSegment.copy(timerStatus = updatedTimerStatus)
            timelineSegments.remove(activeSegment)
            timelineSegments.add(activeSegmentIndex, newActiveSegment)

            // If current segment is completed, update next segment
            if (updatedTimerStatus is TimerStatusDomain.Completed) {
                val nextSegmentIndex = activeSegmentIndex + 1
                val nextSegment = timelineSegments.getOrNull(nextSegmentIndex) ?: return@update pomodoroUi.copy(isComplete = true)
                val timerStatus = nextSegment.timerStatus
                val remainingMillis = timerStatus.durationEpochMs - TIMER_TICK_MILLIS
                val newTimerStatus = TimerStatusDomain.Running(
                        progress = 1 - (remainingMillis.toFloat() / timerStatus.durationEpochMs),
                        remainingMillis = remainingMillis,
                        durationEpochMs = timerStatus.durationEpochMs,
                        formattedTime = remainingMillis.formatDurationMillis(),
                    )

                val newNextSegment = nextSegment.copy(timerStatus = newTimerStatus)
                timelineSegments.removeAt(nextSegmentIndex)
                timelineSegments.add(nextSegmentIndex, newNextSegment)
            }
            pomodoroUi.copy(
                timeline = timeline.copy(segments = timelineSegments.toPersistentList())
            )
        }

    }

    private fun computeElapsedMillis(
        activeSegment: TimelineSegmentUi,
        session: PomodoroSessionUiState,
        now: Long,
    ): Long {
        val baseElapsed = (now - session.startedAtEpochMs).coerceAtLeast(0L)
        val pausedDuration = session.elapsedPauseEpochMs
        val activePause = if (activeSegment.timerStatus is TimerStatusDomain.Paused) {
            val pausedAt = activeSegment.pauseStartedAtEpochMs
            val finalPausedAt = if (pausedAt == 0L) now else pausedAt
            (now - finalPausedAt).coerceAtLeast(0L)
        } else {
            0L
        }
        return (baseElapsed - pausedDuration - activePause).coerceAtLeast(0L)
    }

    internal fun stopTickerForTests() {
        cancelTicker()
    }

    override fun onCleared() {
        cancelTicker()
        super.onCleared()
    }
}
