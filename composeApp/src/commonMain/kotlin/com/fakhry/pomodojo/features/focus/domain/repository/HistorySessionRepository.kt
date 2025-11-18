package com.fakhry.pomodojo.features.focus.domain.repository

import com.fakhry.pomodojo.features.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import com.fakhry.pomodojo.shared.domain.states.DomainResult

interface HistorySessionRepository {
    suspend fun insertHistory(session: PomodoroSessionDomain)
    suspend fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain>
}
