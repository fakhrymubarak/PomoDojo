package com.fakhry.pomodojo.dashboard.domain.model

data class PomodoroHistoryDomain(
    val focusMinutesThisYear: Int,
    val availableYears: List<Int>,
    val histories: List<HistoryDomain>,
)

/**
 * Represents the pomodoro history for a single day.
 *
 * @property date The date of the history entry, formatted as
 * [kotlinx.datetime.LocalDateTime.Formats.ISO]. `2023-01-02T23:40:57.120` is an example of a
 * string in this format.
 * @property focusMinutes The total minutes spent in focus sessions on this date.
 * @property breakMinutes The total minutes spent in break sessions on this date.
 */
data class HistoryDomain(val date: String, val focusMinutes: Int, val breakMinutes: Int)
