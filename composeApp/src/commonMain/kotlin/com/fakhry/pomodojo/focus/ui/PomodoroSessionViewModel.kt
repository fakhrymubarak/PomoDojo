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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

private const val UI_DELAY_UPDATE = 100L

class PomodoroSessionViewModel(
    private val pomodoroSessionRepo: PomodoroSessionRepository,
    private val currentTimeProvider: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val createPomodoroSessionUseCase: CreatePomodoroSessionUseCase,
    private val getPomodoroSessionUseCase: GetActivePomodoroSessionUseCase,
    private val dispatcher: DispatcherProvider,
) : ContainerHost<PomodoroSessionUiState, PomodoroSessionSideEffect>, ViewModel() {
    override val container = container<PomodoroSessionUiState, PomodoroSessionSideEffect>(PomodoroSessionUiState())
    private var countDownJob: Job? = null

    init {
        startNewSession()
    }

    fun onEndClicked() = intent {
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(true))
    }

    // Update activeSegment to Pause
    fun togglePauseResume() = intent {
        viewModelScope.launch(dispatcher.computation) {
            val now = currentTimeProvider.now()
            val activeSegment = state.activeSegment
            val activeTimer = activeSegment.timer
            val updatedTimerStatus = when (activeSegment.timerStatus) {
                TimerStatusDomain.Running -> TimerStatusDomain.Paused
                TimerStatusDomain.Paused -> TimerStatusDomain.Running
                else -> activeSegment.timerStatus
            }

            val timeline = state.timeline
            val timelineSegments = timeline.segments.toMutableList()
            val activeSegmentIndex = timelineSegments.indexOf(activeSegment)
            val newActiveSegment =
                activeSegment.copy(timerStatus = updatedTimerStatus, timer = activeTimer.copy(startedPauseTime = now))
            timelineSegments.remove(activeSegment)
            timelineSegments.add(activeSegmentIndex, newActiveSegment)

            reduce {
                state.copy(
                    activeSegment = newActiveSegment,
                    timeline = timeline.copy(segments = timelineSegments.toPersistentList()),
                )
            }
        }
    }

    fun onDismissConfirmEnd() = intent {
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(false))
    }

    fun onConfirmFinish() = intent {
        postSideEffect(PomodoroSessionSideEffect.OnSessionComplete)
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(false))
    }

    private fun startNewSession() = intent {
        viewModelScope.launch(dispatcher.io) {
            val now = currentTimeProvider.now()
            val session = if (pomodoroSessionRepo.hasActiveSession()) {
                getPomodoroSessionUseCase().toPomodoroUiSessionUi(now)
            } else {
                createPomodoroSessionUseCase(now).toPomodoroUiSessionUi(now)
            }

            reduce { session }

            countDownJob?.cancel()
            startCountDown()
        }
    }

    private fun startCountDown() {
        if (countDownJob?.isActive == true) return
        countDownJob = viewModelScope.launch(dispatcher.computation) {
            while (isActive) {
                delay(UI_DELAY_UPDATE)
                handleTimerTick()
            }
        }
    }

    private fun handleTimerTick() = intent {
        viewModelScope.launch(dispatcher.computation) {
            val now = currentTimeProvider.now()
            val activeSegment = state.activeSegment
            val activeTimer = activeSegment.timer
            val remainingMillis = activeTimer.finishedInMillis - now + activeTimer.elapsedPauseTime
            val isTimerRunning = activeSegment.timerStatus == TimerStatusDomain.Running
            val updatedTimer = activeTimer.copy(
                progress = 1 - (remainingMillis.toFloat() / activeTimer.durationEpochMs),
                finishedInMillis = activeTimer.finishedInMillis,
                formattedTime = remainingMillis.formatDurationMillis(),
                elapsedPauseTime = if (isTimerRunning) {
                    activeTimer.elapsedPauseTime
                } else {
                    now - activeTimer.startedPauseTime
                }
            )
            val updatedTimerStatus = if (isTimerRunning && remainingMillis <= 0) {
                TimerStatusDomain.Completed
            } else {
                activeSegment.timerStatus
            }

            // Update timeline
            val timeline = state.timeline
            val timelineSegments = timeline.segments.toMutableList()
            val activeSegmentIndex = timelineSegments.indexOf(activeSegment)
            val newActiveSegment = activeSegment.copy(timerStatus = updatedTimerStatus, timer = updatedTimer)
            timelineSegments.remove(activeSegment)
            timelineSegments.add(activeSegmentIndex, newActiveSegment)

            reduce {
                state.copy(
                    activeSegment = newActiveSegment,
                    timeline = timeline.copy(segments = timelineSegments.toPersistentList()),
                )
            }

            if (updatedTimerStatus == TimerStatusDomain.Completed) {
                updateRunningTimerToNextSegment(now, activeSegmentIndex, timelineSegments)
            }
        }
    }

    private fun updateRunningTimerToNextSegment(
        now: Long,
        activeSegmentIndex: Int,
        timelineSegments: MutableList<TimelineSegmentUi>,
    ) = intent {
        val nextSegmentIndex = activeSegmentIndex + 1
        val nextSegment = timelineSegments.getOrNull(nextSegmentIndex) ?: return@intent postSideEffect(
            PomodoroSessionSideEffect.OnSessionComplete,
        )
        val nextTimer = nextSegment.timer
        val finishedInMillis = now + nextTimer.durationEpochMs
        val newTimerStatus = TimerStatusDomain.Running
        val newNextSegment = nextSegment.copy(
            timerStatus = newTimerStatus,
            timer = nextTimer.copy(
                progress = 0f,
                finishedInMillis = finishedInMillis,
                formattedTime = nextTimer.durationEpochMs.formatDurationMillis(),
            ),
        )
        timelineSegments.removeAt(nextSegmentIndex)
        timelineSegments.add(nextSegmentIndex, newNextSegment)

        reduce {
            state.copy(
                activeSegment = newNextSegment,
                timeline = state.timeline.copy(segments = timelineSegments.toPersistentList()),
            )
        }
    }
}
