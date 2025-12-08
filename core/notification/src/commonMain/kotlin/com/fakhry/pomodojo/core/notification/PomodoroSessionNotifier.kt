package com.fakhry.pomodojo.core.notification

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain

interface PomodoroSessionNotifier {
    suspend fun schedule(snapshot: PomodoroSessionDomain)

    suspend fun cancel(sessionId: String)
}

object NoOpPomodoroSessionNotifier : PomodoroSessionNotifier {
    override suspend fun schedule(snapshot: PomodoroSessionDomain) = Unit

    override suspend fun cancel(sessionId: String) = Unit
}
