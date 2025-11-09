package com.fakhry.pomodojo.focus.domain.model

import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineTimerDomain

/**
 * Timer status for an active focus session.
 */
enum class FocusTimerStatus {
    RUNNING, PAUSED, FINISHED
}

/**
 * Snapshot of the persisted active session used for restoration.
 */
data class ActiveFocusSessionDomain(
    val sessionId: Long = 0L,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val pauseStartedAtEpochMs: Long? = null,
    val sessionStatus: FocusTimerStatus = FocusTimerStatus.RUNNING,
    val repeatCount: Int = 0,
    val focusMinutes: Int = 0,
    val breakMinutes: Int = 0,
    val longBreakEnabled: Boolean = true,
    val longBreakAfter: Int = 0,
    val longBreakMinutes: Int = 0,
    val quoteId: String = "",
    val timelines: List<TimelineTimerDomain> = emptyList(),
)

data class PomodoroSessionDomain(
    val totalCycle: Int = 0,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val timeline: TimelineDomain = TimelineDomain(),
    val quote: QuoteContent = QuoteContent.DEFAULT_QUOTE,
)