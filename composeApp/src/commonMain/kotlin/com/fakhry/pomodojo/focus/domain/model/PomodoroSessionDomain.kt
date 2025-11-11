package com.fakhry.pomodojo.focus.domain.model

import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain

data class PomodoroSessionDomain(
    val alwaysOnDisplayEnabled: Boolean = true,
    val totalCycle: Int = 0,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val timeline: TimelineDomain = TimelineDomain(),
    val quote: QuoteContent = QuoteContent.DEFAULT_QUOTE,
)
