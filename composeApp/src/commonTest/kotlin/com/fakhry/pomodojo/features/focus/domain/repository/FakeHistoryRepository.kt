package com.fakhry.pomodojo.features.focus.domain.repository

import com.fakhry.pomodojo.commons.domain.state.DomainResult
import com.fakhry.pomodojo.features.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.features.focus.domain.model.PomodoroSessionDomain

class FakeHistoryRepository(
    private val data2024: PomodoroHistoryDomain? = null,
    private val data2023: PomodoroHistoryDomain? = null,
    private val error: Boolean = false,
) : HistorySessionRepository {
    var fetchCount = 0

    override suspend fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain> {
        fetchCount++
        if (error) {
            return DomainResult.Error("not implemented", -1)
        }
        return when (year) {
            2024 -> data2024?.let { DomainResult.Success(it) } ?: DomainResult.Error(
                "No data",
                -1,
            )

            2023 -> data2023?.let { DomainResult.Success(it) } ?: DomainResult.Error(
                "No data",
                -1,
            )

            else -> DomainResult.Error("not implemented", -1)
        }
    }

    override suspend fun insertHistory(session: PomodoroSessionDomain) = Unit
}
