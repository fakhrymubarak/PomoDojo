package com.fakhry.pomodojo.focus.ui

import androidx.compose.runtime.Immutable
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.preferences.ui.model.TimelineUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class PomodoroSessionUiState(
    val phases: ImmutableList<PhaseUi> = persistentListOf(),
    val totalCycle: Int = 0,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val timeline: TimelineUiModel = TimelineUiModel(),
    val quote: QuoteContent = QuoteContent.DEFAULT_QUOTE,
    val isShowConfirmEndDialog: Boolean = false,
) {
    val activePhase : PhaseUi
        get() = phases.firstOrNull { it.timerStatus == PhaseTimerStatus.RUNNING } ?: phases.first()
}

enum class PhaseType {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

enum class PhaseTimerStatus {
    INITIAL, RUNNING, PAUSED, COMPLETED
}

@Immutable
data class PhaseUi(
    val type: PhaseType = PhaseType.FOCUS,
    val cycleNumber: Int = 0,
    val progress: Float = 0f,
    val formattedTime: String = "00:00",
    val timerStatus: PhaseTimerStatus = PhaseTimerStatus.INITIAL,
)

fun List<PhaseUi>.getCurrentCycle() =
    this.indexOfFirst { it.timerStatus == PhaseTimerStatus.RUNNING }
