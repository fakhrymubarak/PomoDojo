package com.fakhry.pomodojo.dashboard.domain.repository

import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.ui.state.DomainResult

interface PomodoroHistoryRepository {
    fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain>
}
