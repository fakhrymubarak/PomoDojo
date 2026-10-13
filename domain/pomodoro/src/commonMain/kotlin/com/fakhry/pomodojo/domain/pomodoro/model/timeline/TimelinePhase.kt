package com.fakhry.pomodojo.domain.pomodoro.model.timeline

/** Where a Timeline stands at a given moment. */
sealed interface TimelinePhase {
    /** A Timeline plan: nothing has started yet. */
    data object NotStarted : TimelinePhase

    data class Running(val index: Int, val remainingMs: Long) : TimelinePhase

    data class Paused(val index: Int, val remainingMs: Long) : TimelinePhase

    /** The Segment at [finishedIndex] ran out; the next one waits for the user to continue. */
    data class AtGate(val finishedIndex: Int) : TimelinePhase

    data object Complete : TimelinePhase
}

fun TimelineDomain.phaseAt(now: Long): TimelinePhase {
    val timeline = elapsedAt(now)
    val index = timeline.segments.indexOfFirst { it.timerStatus != TimerStatusDomain.COMPLETED }
    if (index < 0) return TimelinePhase.Complete
    val segment = timeline.segments[index]
    return when (segment.timerStatus) {
        TimerStatusDomain.RUNNING -> TimelinePhase.Running(index, segment.remainingAt(now))
        TimerStatusDomain.PAUSED -> TimelinePhase.Paused(index, segment.remainingAt(now))
        else -> if (index == 0) TimelinePhase.NotStarted else TimelinePhase.AtGate(index - 1)
    }
}
