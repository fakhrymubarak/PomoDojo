package com.fakhry.pomodojo.focus.domain.usecase

import com.fakhry.pomodojo.focus.domain.model.FocusSessionSnapshot

interface FocusSessionNotifier {
    suspend fun schedule(snapshot: FocusSessionSnapshot)
    suspend fun cancel(sessionId: String)
}

object NoOpFocusSessionNotifier : FocusSessionNotifier {
    override suspend fun schedule(snapshot: FocusSessionSnapshot) = Unit
    override suspend fun cancel(sessionId: String) = Unit
}
