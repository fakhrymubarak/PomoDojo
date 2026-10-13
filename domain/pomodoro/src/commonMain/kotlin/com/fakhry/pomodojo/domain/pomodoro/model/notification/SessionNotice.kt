package com.fakhry.pomodojo.domain.pomodoro.model.notification

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelinePhase
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.phaseAt

/**
 * What the platform notification shows for a Session at one moment. Adapters only render it, and
 * arm their own alarm or chime from [Running.endsAtMs]; nothing else is pending.
 */
sealed interface SessionNotice {
    val sessionId: String

    /** A notice with a Segment on screen. */
    sealed interface Showing : SessionNotice {
        val segment: NoticeSegment
        val totalCycle: Int
        val quote: QuoteContent
        val remainingMs: Long
    }

    data class Running(
        override val sessionId: String,
        override val segment: NoticeSegment,
        override val totalCycle: Int,
        override val quote: QuoteContent,
        override val remainingMs: Long,
        val endsAtMs: Long,
    ) : Showing

    data class Paused(
        override val sessionId: String,
        override val segment: NoticeSegment,
        override val totalCycle: Int,
        override val quote: QuoteContent,
        override val remainingMs: Long,
    ) : Showing

    /** [segment] ran out; [next] waits at the Gate for the user to continue. */
    data class AtGate(
        override val sessionId: String,
        override val segment: NoticeSegment,
        override val totalCycle: Int,
        override val quote: QuoteContent,
        val next: NoticeSegment,
    ) : Showing {
        override val remainingMs: Long get() = 0L
    }

    /** Nothing to show: the Session is complete, finished or gone. */
    data class Cleared(override val sessionId: String) : SessionNotice
}

data class NoticeSegment(val type: TimerType, val cycleNumber: Int, val durationMs: Long)

fun PomodoroSessionDomain.toNotice(now: Long): SessionNotice {
    val segments = timeline.segments
    return when (val phase = timeline.phaseAt(now)) {
        is TimelinePhase.Running -> SessionNotice.Running(
            sessionId = sessionId(),
            segment = segments[phase.index].toNoticeSegment(),
            totalCycle = totalCycle,
            quote = quote,
            remainingMs = phase.remainingMs,
            endsAtMs = now + phase.remainingMs,
        )

        is TimelinePhase.Paused -> SessionNotice.Paused(
            sessionId = sessionId(),
            segment = segments[phase.index].toNoticeSegment(),
            totalCycle = totalCycle,
            quote = quote,
            remainingMs = phase.remainingMs,
        )

        is TimelinePhase.AtGate -> SessionNotice.AtGate(
            sessionId = sessionId(),
            segment = segments[phase.finishedIndex].toNoticeSegment(),
            totalCycle = totalCycle,
            quote = quote,
            next = segments[phase.finishedIndex + 1].toNoticeSegment(),
        )

        TimelinePhase.NotStarted, TimelinePhase.Complete -> SessionNotice.Cleared(sessionId())
    }
}

private fun TimerSegmentsDomain.toNoticeSegment() =
    NoticeSegment(type = type, cycleNumber = cycleNumber, durationMs = timer.durationEpochMs)
