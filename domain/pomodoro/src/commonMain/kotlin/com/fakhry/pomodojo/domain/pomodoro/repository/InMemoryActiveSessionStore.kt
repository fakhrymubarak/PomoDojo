package com.fakhry.pomodojo.domain.pomodoro.repository

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.updateAndGet

/** An [ActiveSessionStore] held in memory, for tests. */
class InMemoryActiveSessionStore(initial: PomodoroSessionDomain? = null) : ActiveSessionStore {
    private val state = MutableStateFlow(initial)

    override val session: Flow<PomodoroSessionDomain?> = state

    override suspend fun update(
        transform: (PomodoroSessionDomain?) -> PomodoroSessionDomain?,
    ): PomodoroSessionDomain? = state.updateAndGet(transform)

    override suspend fun take(): PomodoroSessionDomain? = state.getAndUpdate { null }
}
