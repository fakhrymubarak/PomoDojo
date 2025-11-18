package com.fakhry.pomodojo.shared.domain.model.focus

import com.fakhry.pomodojo.shared.domain.model.quote.QuoteContent
import com.fakhry.pomodojo.shared.domain.model.timeline.TimelineDomain

data class PomodoroSessionDomain(
    val totalCycle: Int = 0,
    val startedAtEpochMs: Long = 0L,
    val elapsedPauseEpochMs: Long = 0L,
    val timeline: TimelineDomain = TimelineDomain(),
    val quote: QuoteContent = QuoteContent.Companion.DEFAULT_QUOTE,
) {
    fun sessionId() = startedAtEpochMs.toString()
}
