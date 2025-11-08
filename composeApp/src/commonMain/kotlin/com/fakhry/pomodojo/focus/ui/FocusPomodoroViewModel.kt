package com.fakhry.pomodojo.focus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.focus.domain.model.ActiveFocusSessionDomain
import com.fakhry.pomodojo.focus.domain.model.ActiveFocusSessionWithQuoteDomain
import com.fakhry.pomodojo.focus.domain.model.FocusTimerStatus
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.usecase.GetActivePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.SystemCurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.BuildFocusTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.ui.mapper.mapToTimelineSegmentsUi
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FocusPomodoroViewModel(
    private val pomodoroSessionRepo: PomodoroSessionRepository,
    private val currentTimeProvider: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val notifier: FocusSessionNotifier,
    private val createPomodoroSessionUseCase: CreatePomodoroSessionUseCase,
    private val getPomodoroSessionUseCase: GetActivePomodoroSessionUseCase,
    private val timelineBuilder: BuildFocusTimelineUseCase,
    private val hourSplitter: BuildHourSplitTimelineUseCase,
    private val dispatcher: DispatcherProvider,
) : ViewModel() {

    private val _isComplete = MutableStateFlow(false)
    val isComplete = _isComplete.asStateFlow()

    private val _state = MutableStateFlow(PomodoroSessionUiState())
    val state = _state.asStateFlow()

    init {
        startPomodoroSession()
    }

    private fun startPomodoroSession() = viewModelScope.launch(dispatcher.io) {
        val session = if (pomodoroSessionRepo.hasActiveSession()) {
            getPomodoroSessionUseCase().toPomodoroTimerUi()
        } else {
            createPomodoroSessionUseCase().toNewPomodoroTimerUi()
        }
        _state.update { session }
    }

    private fun ActiveFocusSessionWithQuoteDomain.toNewPomodoroTimerUi(): PomodoroSessionUiState {
        val remainingSeconds = focusSession.focusMinutes * 60
        return PomodoroSessionUiState(
            startedAtEpochMs = focusSession.startedAtEpochMs,
            elapsedPauseEpochMs = focusSession.elapsedPauseEpochMs,
            formattedTime = formatDuration(remainingSeconds),
            timeline = TimelineUiModel(
                segments = timelineBuilder(preferences).mapToTimelineSegmentsUi(),
                hourSplits = hourSplitter(preferences).toPersistentList(),
            ),
            quote = quote,
        )
    }


    private fun ActiveFocusSessionWithQuoteDomain.toPomodoroTimerUi(): PomodoroSessionUiState {
        @OptIn(ExperimentalTime::class)
        val currentTime = Clock.System.now().toEpochMilliseconds()

        val cycles = buildCycleRangeMinutes(preferences, focusSession.startedAtEpochMs)
        val currentCycle = cycles.indexOfFirst { range -> currentTime in range }

        // TODO CODEX, CREATE LOGIC TO GET CURRENT PHASE

        return PomodoroSessionUiState(
            startedAtEpochMs = focusSession.startedAtEpochMs,
            elapsedPauseEpochMs = focusSession.elapsedPauseEpochMs,
//            formattedTime = formatDuration(remainingSeconds),
            timeline = TimelineUiModel(
                segments = timelineBuilder(preferences).mapToTimelineSegmentsUi(),
                hourSplits = hourSplitter(preferences).toPersistentList(),
            ),
            quote = quote,
        )
    }

    fun togglePauseResume() = viewModelScope.launch {
    }

    fun onEndClicked() {
        _state.update { it.copy(isShowConfirmEndDialog = true) }
    }

    fun onDismissConfirmEnd() {
        _state.update { it.copy(isShowConfirmEndDialog = false) }
    }

    fun onConfirmFinish() {
    }

    fun decrementTimer() {

    }

    private suspend fun completeSnapshot(snapshot: ActiveFocusSessionDomain) {
        pomodoroSessionRepo.completeSession(snapshot)
        withContext(dispatcher.main) {
        }
    }

    private fun restoreSnapshot(snapshot: ActiveFocusSessionDomain) {
        if (snapshot.sessionStatus == FocusTimerStatus.RUNNING) {
            val now = currentTimeProvider.now()

        } else {

        }
    }

    private fun buildCycleRangeMinutes(
        preferences: PreferencesDomain,
        startEpochMillis: Long,
    ): List<LongRange> {
        val ranges = mutableListOf<LongRange>()
        var startRange = startEpochMillis
        for (cycle in 1..preferences.repeatCount) {
            val isLastFocus = cycle == preferences.repeatCount

            when {
                // Long Break
                !isLastFocus && preferences.isLongBreakPoint -> {
                    startRange += preferences.longBreakMillis
                }

                // Short Break
                !isLastFocus -> {
                    startRange += preferences.breakMillis
                }

                // Focus
                else -> {
                    startRange += preferences.focusMillis
                    ranges.add(LongRange(startRange, preferences.focusMillis))
                }
            }
        }
        return ranges
    }

    private fun formatDuration(remainingSeconds: Int): String {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        return "$minutes:$seconds"
    }

}
