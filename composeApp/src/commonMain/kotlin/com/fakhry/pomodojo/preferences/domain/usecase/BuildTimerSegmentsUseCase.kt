package com.fakhry.pomodojo.preferences.domain.usecase

import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType

private const val MINUTES_IN_MILLIS = 60_000L

class BuildTimerSegmentsUseCase {
    operator fun invoke(
        now: Long,
        preferences: PreferencesDomain,
    ): List<TimerSegmentsDomain> {
        val segments = mutableListOf<TimerSegmentsDomain>()
        for (cycle in 1..preferences.repeatCount) {
            segments +=
                TimerSegmentsDomain(
                    type = TimerType.FOCUS,
                    cycleNumber = cycle,
                    timer =
                        TimerDomain(
                            durationEpochMs = preferences.focusMinutes * MINUTES_IN_MILLIS,
                        ),
                )
            val isLongBreakPoint =
                preferences.longBreakEnabled && cycle % preferences.longBreakAfter == 0
            val isLastFocus = cycle == preferences.repeatCount

            if (!isLastFocus && isLongBreakPoint) {
                segments +=
                    TimerSegmentsDomain(
                        type = TimerType.LONG_BREAK,
                        cycleNumber = cycle,
                        timer =
                            TimerDomain(
                                durationEpochMs = preferences.longBreakMinutes * MINUTES_IN_MILLIS,
                            ),
                    )
            } else if (!isLastFocus) {
                segments +=
                    TimerSegmentsDomain(
                        type = TimerType.SHORT_BREAK,
                        cycleNumber = cycle,
                        timer =
                            TimerDomain(
                                durationEpochMs = preferences.breakMinutes * MINUTES_IN_MILLIS,
                            ),
                    )
            }
        }

        return segments.setFirstSegmentRunning(now)
    }

    private fun MutableList<TimerSegmentsDomain>.setFirstSegmentRunning(now: Long): List<TimerSegmentsDomain> {
        val first = first()
        val firstSegment =
            first.copy(
                timer =
                    first.timer.copy(
                        finishedInMillis = now + first.timer.durationEpochMs,
                    ),
                timerStatus = TimerStatusDomain.Running,
            )

        remove(first)
        add(0, firstSegment)
        return this
    }
}
