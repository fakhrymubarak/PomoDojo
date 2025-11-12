package com.fakhry.pomodojo.focus.domain.repository

import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.ui.state.DomainResult

interface HistorySessionRepository {
    fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain>
}
