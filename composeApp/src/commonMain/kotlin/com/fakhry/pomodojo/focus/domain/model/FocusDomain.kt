package com.fakhry.pomodojo.focus.domain.model

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Represents the current phase of a pomodoro cycle.
 */
enum class FocusPhase {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK,
}

/**
 * Timer status for an active focus session.
 */
enum class FocusTimerStatus {
    RUNNING,
    PAUSED,
}

/**
 * Shared configuration needed to start a pomodoro session.
 */
data class FocusSessionConfig(
    val focusDurationMinutes: Int,
    val shortBreakMinutes: Int,
    val longBreakMinutes: Int,
    val totalCycles: Int,
    val autoStartNextPhase: Boolean,
    val autoStartBreaks: Boolean,
) {
    companion object {
        fun fromPreferences(preferences: PreferencesDomain): FocusSessionConfig = FocusSessionConfig(
            focusDurationMinutes = preferences.focusMinutes,
            shortBreakMinutes = preferences.breakMinutes,
            longBreakMinutes = if (preferences.longBreakEnabled) {
                preferences.longBreakMinutes
            } else {
                0
            },
            totalCycles = preferences.repeatCount,
            autoStartNextPhase = false,
            autoStartBreaks = false,
        )
    }
}

/**
 * Quote displayed during a focus session.
 */
data class QuoteContent(
    val id: String,
    val text: String,
    val character: String?,
    val sourceTitle: String?,
    val metadata: String?,
)

/**
 * Snapshot of the persisted active session used for restoration.
 */
data class FocusSessionSnapshot(
    val sessionId: String,
    val status: FocusTimerStatus,
    val focusDurationMinutes: Int,
    val shortBreakMinutes: Int,
    val longBreakMinutes: Int,
    val autoStartNextPhase: Boolean,
    val autoStartBreaks: Boolean,
    val phaseRemainingSeconds: Int,
    val currentPhaseTotalSeconds: Int,
    val completedCycles: Int,
    val totalCycles: Int,
    val phase: FocusPhase,
    val phaseStartedAtEpochMs: Long,
    val quote: QuoteContent?,
    val startedAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

fun interface SessionIdGenerator {
    fun nextId(): String
}

@OptIn(ExperimentalTime::class)
val DefaultSessionIdGenerator = SessionIdGenerator {
    val timestamp = Clock.System.now().toEpochMilliseconds()
    "session-$timestamp"
}
