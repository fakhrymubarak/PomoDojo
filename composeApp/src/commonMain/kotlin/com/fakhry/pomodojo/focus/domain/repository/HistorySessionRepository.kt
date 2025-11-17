package com.fakhry.pomodojo.focus.domain.repository

import com.fakhry.pomodojo.commons.domain.state.DomainResult
import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain

interface HistorySessionRepository {
    suspend fun insertHistory(session: PomodoroSessionDomain)
    suspend fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain>
}
