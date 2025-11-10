package com.fakhry.pomodojo.dashboard.domain.model

data class PomodoroHistoryDomain(
    val focusMinutesThisYear: Int,
    val histories: List<HistoryDomain>,
)

data class HistoryDomain(
    val date: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
)
