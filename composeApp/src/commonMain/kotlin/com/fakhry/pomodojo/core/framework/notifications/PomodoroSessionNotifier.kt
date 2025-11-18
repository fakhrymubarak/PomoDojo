package com.fakhry.pomodojo.core.framework.notifications

import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain

expect fun providePomodoroSessionNotifier(): PomodoroSessionNotifier

interface PomodoroSessionNotifier {
    suspend fun schedule(snapshot: PomodoroSessionDomain)

    suspend fun cancel(sessionId: String)
}

object NoOpPomodoroSessionNotifier : PomodoroSessionNotifier {
    override suspend fun schedule(snapshot: PomodoroSessionDomain) = Unit

    override suspend fun cancel(sessionId: String) = Unit
}
