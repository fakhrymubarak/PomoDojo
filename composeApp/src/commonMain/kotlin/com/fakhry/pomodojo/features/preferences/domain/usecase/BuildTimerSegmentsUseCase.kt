package com.fakhry.pomodojo.features.preferences.domain.usecase

import com.fakhry.pomodojo.core.utils.constant.Time
import com.fakhry.pomodojo.features.preferences.domain.model.PomodoroPreferences
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerType

class BuildTimerSegmentsUseCase {
    operator fun invoke(now: Long, preferences: PomodoroPreferences): List<TimerSegmentsDomain> {
        val segments = mutableListOf<TimerSegmentsDomain>()
        for (cycle in 1..preferences.repeatCount) {
            segments += TimerSegmentsDomain(
                type = TimerType.FOCUS,
                cycleNumber = cycle,
                timer = TimerDomain(
                    durationEpochMs = preferences.focusMinutes * Time.MILLIS_PER_MINUTE,
                ),
            )
            val isLongBreakPoint =
                preferences.longBreakEnabled && cycle % preferences.longBreakAfter == 0

            if (isLongBreakPoint) {
                segments += TimerSegmentsDomain(
                    type = TimerType.LONG_BREAK,
                    cycleNumber = cycle,
                    timer = TimerDomain(
                        durationEpochMs = preferences.longBreakMinutes * Time.MILLIS_PER_MINUTE,
                    ),
                )
            } else {
                segments += TimerSegmentsDomain(
                    type = TimerType.SHORT_BREAK,
                    cycleNumber = cycle,
                    timer = TimerDomain(
                        durationEpochMs = preferences.breakMinutes * Time.MILLIS_PER_MINUTE,
                    ),
                )
            }
        }

        return segments.setFirstSegmentRunning(now)
    }

    private fun MutableList<TimerSegmentsDomain>.setFirstSegmentRunning(
        now: Long,
    ): List<TimerSegmentsDomain> {
        val first = first()
        val firstSegment = first.copy(
            timer = first.timer.copy(
                finishedInMillis = now + first.timer.durationEpochMs,
            ),
            timerStatus = TimerStatusDomain.RUNNING,
        )

        remove(first)
        add(0, firstSegment)
        return this
    }
}
