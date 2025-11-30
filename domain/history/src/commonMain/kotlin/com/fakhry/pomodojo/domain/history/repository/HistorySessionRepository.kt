package com.fakhry.pomodojo.domain.history.repository

import com.fakhry.pomodojo.domain.common.DomainResult
import com.fakhry.pomodojo.domain.history.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain

interface HistorySessionRepository {
    suspend fun insertHistory(session: PomodoroSessionDomain)
    suspend fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain>
}
