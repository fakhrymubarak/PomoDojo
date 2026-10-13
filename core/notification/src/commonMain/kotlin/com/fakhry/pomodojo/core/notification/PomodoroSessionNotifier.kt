package com.fakhry.pomodojo.core.notification

import com.fakhry.pomodojo.domain.pomodoro.model.notification.SessionNotice

interface PomodoroSessionNotifier {
    suspend fun show(notice: SessionNotice)
}

object NoOpPomodoroSessionNotifier : PomodoroSessionNotifier {
    override suspend fun show(notice: SessionNotice) = Unit
}
