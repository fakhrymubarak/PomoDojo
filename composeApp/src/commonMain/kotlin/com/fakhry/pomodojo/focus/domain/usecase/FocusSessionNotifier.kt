package com.fakhry.pomodojo.focus.domain.usecase

import com.fakhry.pomodojo.focus.domain.model.ActiveFocusSessionDomain

interface FocusSessionNotifier {
    suspend fun schedule(snapshot: ActiveFocusSessionDomain)

    suspend fun cancel(sessionId: String)
}

object NoOpFocusSessionNotifier : FocusSessionNotifier {
    override suspend fun schedule(snapshot: ActiveFocusSessionDomain) = Unit

    override suspend fun cancel(sessionId: String) = Unit
}
