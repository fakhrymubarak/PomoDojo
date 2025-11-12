package com.fakhry.pomodojo.focus.domain.repository

import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.ui.state.DomainResult

interface HistorySessionRepository {
    suspend fun insertHistory(session: PomodoroSessionDomain)
    suspend fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain>
}
