package com.fakhry.pomodojo.focus.domain.usecase

import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain

interface FocusSessionNotifier {
    suspend fun schedule(snapshot: PomodoroSessionDomain)

    suspend fun cancel(sessionId: String)
}

object NoOpFocusSessionNotifier : FocusSessionNotifier {
    override suspend fun schedule(snapshot: PomodoroSessionDomain) = Unit

    override suspend fun cancel(sessionId: String) = Unit
}
