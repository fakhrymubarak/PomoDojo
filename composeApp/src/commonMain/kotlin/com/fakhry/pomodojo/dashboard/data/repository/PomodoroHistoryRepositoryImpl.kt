package com.fakhry.pomodojo.dashboard.data.repository

import com.fakhry.pomodojo.dashboard.domain.model.HistoryDomain
import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.dashboard.domain.repository.PomodoroHistoryRepository
import com.fakhry.pomodojo.ui.state.DomainResult

class PomodoroHistoryRepositoryImpl : PomodoroHistoryRepository {

    override fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain> {
        // 144 dummy entries: years 2023..2026, months 1..12, days 1/10/20 (clamped to month length)
        val histories: List<HistoryDomain> = buildList {
            for (y in 2023..2025) {
                for (m in 1..12) {
                    val dim = daysInMonth(y, m)
                    val days = (1..28).toList()
                    for (d in days) {
                        val day = if (d <= dim) d else dim
                        val dateStr = formatDate(day, m, y)

                        // Deterministic pseudo-random-ish values (multiples of 25)
                        val focus = (((y * 37 + m * 13 + day) % 9) + 2) * 25   // 50..275
                        val breaks = focus / 5

                        add(HistoryDomain(dateStr, focus, breaks))
                    }
                }
            }
        }

        // Filter by requested year (no date libs needed)
        val filtered = histories.filter { extractYear(it.date) == year }

        // Aggregate total focus minutes for the year
        val focusMinutesThisYear = filtered.sumOf { it.focusMinutes }

        return DomainResult.Success(
            PomodoroHistoryDomain(
                focusMinutesThisYear = focusMinutesThisYear,
                histories = filtered
            )
        )
    }

    /** Helpers (KMP-safe, no java.time or String.format) */

    private fun formatDate(day: Int, month: Int, year: Int): String {
        val d = day.toString().padStart(2, '0')
        val m = month.toString().padStart(2, '0')
        val y = year.toString().padStart(4, '0')
        return "$d-$m-$y"
    }

    /** Helpers (no java.time) */
    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> error("Invalid month: $month")
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    private fun extractYear(date: String): Int =
        date.substring(date.lastIndexOf('-') + 1).toIntOrNull()
            ?: error("Invalid date format (expected dd-MM-yyyy): $date")
}
