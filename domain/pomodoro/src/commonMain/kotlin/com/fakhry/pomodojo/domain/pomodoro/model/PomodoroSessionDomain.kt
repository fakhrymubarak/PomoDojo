package com.fakhry.pomodojo.domain.pomodoro.model

import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain

data class PomodoroSessionDomain(
    val totalCycle: Int = 0,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val timeline: TimelineDomain = TimelineDomain(),
    val quote: QuoteContent = QuoteContent.Companion.DEFAULT_QUOTE,
) {
    fun sessionId() = startedAtEpochMs.toString()
}
