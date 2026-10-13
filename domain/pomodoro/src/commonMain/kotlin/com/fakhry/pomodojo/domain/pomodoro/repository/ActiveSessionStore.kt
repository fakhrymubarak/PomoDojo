package com.fakhry.pomodojo.domain.pomodoro.repository

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import kotlinx.coroutines.flow.Flow

/**
 * The stored active Session; `null` when there is none. The app and the background segment alarm
 * both write it, so every write is an atomic read-modify-write: neither can overwrite a change
 * the other made in between.
 */
interface ActiveSessionStore {
    val session: Flow<PomodoroSessionDomain?>

    /** Replaces the Session with [transform] of the stored one and returns what was stored. */
    suspend fun update(
        transform: (PomodoroSessionDomain?) -> PomodoroSessionDomain?,
    ): PomodoroSessionDomain?

    /** Removes and returns the Session. Of concurrent callers, only one gets it. */
    suspend fun take(): PomodoroSessionDomain?
}
