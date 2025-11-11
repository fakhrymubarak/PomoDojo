package com.fakhry.pomodojo.focus.domain.model

enum class FocusTimerStatus {
    RUNNING,
    PAUSED,
    COMPLETED,
}

data class ActiveFocusSessionDomain(
    val sessionId: String,
    val status: FocusTimerStatus = FocusTimerStatus.RUNNING,
    val updatedAtEpochMs: Long = 0L,
    val phaseRemainingSeconds: Long = 0L,
)
