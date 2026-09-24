package com.fakhry.pomodojo.domain.pomodoro.model.timeline

/**
 * Detects the "awaiting continue" gate state used between pomodoro phases.
 *
 * After a phase completes, the timeline is left with the finished segment marked
 * [TimerStatusDomain.COMPLETED] while the following segment stays
 * [TimerStatusDomain.INITIAL] (i.e. not auto-started). That resting shape is the
 * gate: the timer is parked, waiting for the user to continue to the next phase.
 *
 * @return the index of the pending [TimerStatusDomain.INITIAL] segment that is
 * waiting to be started, or `null` when there is no gate open (brand-new session,
 * a segment is actively running/paused, or the whole session is complete).
 */
fun List<TimerSegmentsDomain>.awaitingContinueIndex(): Int? {
    val firstPending = indexOfFirst { it.timerStatus != TimerStatusDomain.COMPLETED }
    if (firstPending <= 0) return null
    return firstPending.takeIf { this[it].timerStatus == TimerStatusDomain.INITIAL }
}
