package com.fakhry.pomodojo.domain.pomodoro.model.notification

import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.continuedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.elapsedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.pausedAt
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.started
import kotlin.test.Test
import kotlin.test.assertEquals

private const val MINUTE = 60_000L
private const val START = 1_000_000L

private val FOCUS = NoticeSegment(TimerType.FOCUS, cycleNumber = 1, durationMs = 25 * MINUTE)
private val BREAK = NoticeSegment(TimerType.SHORT_BREAK, cycleNumber = 1, durationMs = 5 * MINUTE)

class SessionNoticeTest {
    private val plan = PomodoroSessionDomain(
        totalCycle = 1,
        startedAtEpochMs = START,
        timeline = TimelineDomain(
            segments = listOf(segment(FOCUS), segment(BREAK)),
        ),
    )

    @Test
    fun `running notice ends when the segment does`() {
        val session = plan.timed { started(START) }

        assertEquals(
            SessionNotice.Running(
                sessionId = session.sessionId(),
                segment = FOCUS,
                totalCycle = 1,
                quote = session.quote,
                remainingMs = 15 * MINUTE,
                endsAtMs = START + 25 * MINUTE,
            ),
            session.toNotice(START + 10 * MINUTE),
        )
    }

    @Test
    fun `paused notice keeps the remaining time`() {
        val session = plan.timed { started(START).pausedAt(START + 10 * MINUTE) }

        val notice = session.toNotice(START + 60 * MINUTE)

        assertEquals(SessionNotice.Paused::class, notice::class)
        assertEquals(15 * MINUTE, (notice as SessionNotice.Paused).remainingMs)
    }

    @Test
    fun `segment that ran out shows the gate with the next segment`() {
        val session = plan.timed { started(START) }

        val notice = session.toNotice(START + 25 * MINUTE)

        assertEquals(
            SessionNotice.AtGate(
                sessionId = session.sessionId(),
                segment = FOCUS,
                totalCycle = 1,
                quote = session.quote,
                next = BREAK,
            ),
            notice,
        )
    }

    @Test
    fun `unstarted or complete session clears the notice`() {
        val complete = plan.timed {
            started(START).elapsedAt(START + 25 * MINUTE).continuedAt(START + 25 * MINUTE)
        }

        assertEquals(SessionNotice.Cleared(plan.sessionId()), plan.toNotice(START))
        assertEquals(
            SessionNotice.Cleared(plan.sessionId()),
            complete.toNotice(START + 30 * MINUTE),
        )
    }
}

private fun PomodoroSessionDomain.timed(change: TimelineDomain.() -> TimelineDomain) =
    copy(timeline = timeline.change())

private fun segment(notice: NoticeSegment) = TimerSegmentsDomain(
    type = notice.type,
    cycleNumber = notice.cycleNumber,
    timer = TimerDomain(durationEpochMs = notice.durationMs),
)
