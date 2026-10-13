package com.fakhry.pomodojo.domain.pomodoro.model.timeline

/*
 * Timeline transitions. Each takes the moment it happens (epoch ms) and returns the new Timeline;
 * a transition that does not apply where the Timeline stands returns it unchanged.
 */

fun TimelineDomain.started(now: Long): TimelineDomain =
    if (phaseAt(now) == TimelinePhase.NotStarted) runSegment(0, now) else this

/** Completes a running Segment whose time ran out, leaving the Timeline at the Gate. */
fun TimelineDomain.elapsedAt(now: Long): TimelineDomain {
    val index = segments.indexOfFirst { it.timerStatus == TimerStatusDomain.RUNNING }
    if (index < 0 || segments[index].remainingAt(now) > 0L) return this
    return completeSegment(index, credit = 1f)
}

fun TimelineDomain.pausedAt(now: Long): TimelineDomain {
    val timeline = elapsedAt(now)
    val phase = timeline.phaseAt(now) as? TimelinePhase.Running ?: return timeline
    return timeline.updateSegment(phase.index) {
        copy(
            timer = timer.copy(startedPauseTime = now),
            timerStatus = TimerStatusDomain.PAUSED,
        )
    }
}

fun TimelineDomain.resumedAt(now: Long): TimelineDomain {
    val phase = phaseAt(now) as? TimelinePhase.Paused ?: return this
    return updateSegment(phase.index) {
        copy(
            timer = timer.copy(
                finishedInMillis = now + phase.remainingMs,
                startedPauseTime = 0L,
            ),
            timerStatus = TimerStatusDomain.RUNNING,
        )
    }
}

fun TimelineDomain.continuedAt(now: Long): TimelineDomain {
    val phase = phaseAt(now) as? TimelinePhase.AtGate ?: return this
    return elapsedAt(now).runSegment(phase.finishedIndex + 1, now)
}

/** Ends the current Segment early, crediting the time it ran, and starts the next one. */
fun TimelineDomain.skippedAt(now: Long): TimelineDomain = finishedAt(now).continuedAt(now)

/** Ends the current Segment early, crediting the time it ran. Later Segments stay unstarted. */
fun TimelineDomain.finishedAt(now: Long): TimelineDomain {
    val timeline = elapsedAt(now)
    val index = when (val phase = timeline.phaseAt(now)) {
        is TimelinePhase.Running -> phase.index
        is TimelinePhase.Paused -> phase.index
        else -> return timeline
    }
    return timeline.completeSegment(index, timeline.segments[index].progressAt(now))
}

private fun TimelineDomain.runSegment(index: Int, now: Long) = updateSegment(index) {
    copy(
        timer = timer.copy(
            progress = 0f,
            finishedInMillis = now + timer.durationEpochMs,
            startedPauseTime = 0L,
        ),
        timerStatus = TimerStatusDomain.RUNNING,
    )
}

private fun TimelineDomain.completeSegment(index: Int, credit: Float) = updateSegment(index) {
    copy(
        timer = timer.copy(progress = credit.coerceIn(0f, 1f), startedPauseTime = 0L),
        timerStatus = TimerStatusDomain.COMPLETED,
    )
}

private fun TimelineDomain.updateSegment(
    index: Int,
    transform: TimerSegmentsDomain.() -> TimerSegmentsDomain,
) = copy(segments = segments.toMutableList().also { it[index] = it[index].transform() })
