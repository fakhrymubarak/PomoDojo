package com.fakhry.pomodojo.focus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.GetActivePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.SystemCurrentTimeProvider
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

private const val TIMER_TICK_MILLIS = 5_000L

class PomodoroSessionViewModel(
    private val pomodoroSessionRepo: PomodoroSessionRepository,
    private val currentTimeProvider: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val createPomodoroSessionUseCase: CreatePomodoroSessionUseCase,
    private val getPomodoroSessionUseCase: GetActivePomodoroSessionUseCase,
    private val dispatcher: DispatcherProvider,
) : ContainerHost<PomodoroSessionUiState, PomodoroSessionSideEffect>, ViewModel() {

    override val container =
        container<PomodoroSessionUiState, PomodoroSessionSideEffect>(PomodoroSessionUiState())

    init {
        startNewSession()
    }

    fun startNewSession() = intent {
        viewModelScope.launch(dispatcher.io) {
            val now = currentTimeProvider.now()
            val session = if (pomodoroSessionRepo.hasActiveSession()) {
                getPomodoroSessionUseCase().toPomodoroUiSessionUi()
            } else {
                createPomodoroSessionUseCase(now).toPomodoroUiSessionUi()
            }

            reduce { session }
        }
    }

    fun onEndClicked() = intent {
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(true))
    }

    fun togglePauseResume() {
        TODO("Not yet implemented")
    }


    fun onDismissConfirmEnd() = intent {
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(false))

    }


    fun onConfirmFinish() = intent {
        postSideEffect(PomodoroSessionSideEffect.OnSessionComplete)
        postSideEffect(PomodoroSessionSideEffect.ShowEndSessionDialog(false))
    }

}
