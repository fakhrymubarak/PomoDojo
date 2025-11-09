package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineTimerDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.utils.formatDurationMillis

class BuildFocusTimelineUseCase {

    operator fun invoke(now: Long, preferences: PreferencesDomain): List<TimelineTimerDomain> {
        val segments = mutableListOf<TimelineTimerDomain>()
        for (cycle in 1..preferences.repeatCount) {
            segments += TimelineTimerDomain(
                type = TimerType.FOCUS,
                cycleNumber = cycle,
                timerStatus = TimerStatusDomain.Initial(
                    durationEpochMs = preferences.focusMinutes * 60_000L
                ),
            )
            val isLongBreakPoint =
                preferences.longBreakEnabled && cycle % preferences.longBreakAfter == 0
            val isLastFocus = cycle == preferences.repeatCount

            if (!isLastFocus && isLongBreakPoint) {
                segments += TimelineTimerDomain(
                    type = TimerType.LONG_BREAK,
                    cycleNumber = cycle,
                    timerStatus = TimerStatusDomain.Initial(
                        durationEpochMs = preferences.longBreakMinutes * 60_000L
                    )
                )
            } else if (!isLastFocus) {
                segments += TimelineTimerDomain(
                    type = TimerType.SHORT_BREAK,
                    cycleNumber = cycle,
                    timerStatus = TimerStatusDomain.Initial(
                        durationEpochMs = preferences.breakMinutes * 60_000L
                    )
                )
            }
        }

        return segments.setFirstSegmentRunning(now)
    }

    private fun MutableList<TimelineTimerDomain>.setFirstSegmentRunning(now: Long): List<TimelineTimerDomain> {
        val first = first()
        val firstSegment = first.copy(
            timerStatus = TimerStatusDomain.Running(
                progress = 0f,
                durationEpochMs = first.timerStatus.durationEpochMs,
                formattedTime = first.timerStatus.durationEpochMs.formatDurationMillis(),
                remainingMillis = first.timerStatus.durationEpochMs,
                startedAtEpochMs = now,
            )
        )

        remove(first)
        add(0, firstSegment)
        return this
    }
}
