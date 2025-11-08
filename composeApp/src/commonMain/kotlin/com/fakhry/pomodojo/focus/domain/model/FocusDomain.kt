package com.fakhry.pomodojo.focus.domain.model

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineSegmentDomain

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
    val sessionStatus: FocusTimerStatus = FocusTimerStatus.RUNNING,
    val repeatCount: Int = 0,
    val focusMinutes: Int = 0,
    val breakMinutes: Int = 0,
    val longBreakEnabled: Boolean = true,
    val longBreakAfter: Int = 0,
    val longBreakMinutes: Int = 0,
    val quoteId: String = "",
    val timelines: List<TimelineSegmentDomain> = emptyList(),
)


data class ActiveFocusSessionWithQuoteDomain(
    val focusSession: ActiveFocusSessionDomain,
    val quote: QuoteContent,
    val preferences: PreferencesDomain,
)