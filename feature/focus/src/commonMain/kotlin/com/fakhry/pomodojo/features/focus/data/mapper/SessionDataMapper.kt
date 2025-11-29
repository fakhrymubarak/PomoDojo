package com.fakhry.pomodojo.features.focus.data.mapper

import com.fakhry.pomodojo.core.database.entities.HistorySessionEntity
import com.fakhry.pomodojo.core.utils.primitives.toMinutes
import com.fakhry.pomodojo.shared.domain.model.focus.PomodoroSessionDomain
import com.fakhry.pomodojo.shared.domain.model.history.HistoryDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.shared.domain.model.timeline.TimerType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun PomodoroSessionDomain.toHistoryEntity(): HistorySessionEntity {
    val elapsedSegments = timeline.segments.filterNot {
        it.timerStatus == TimerStatusDomain.INITIAL
    }
    val totalFocusMinutes = elapsedSegments
        .filter { it.type == TimerType.FOCUS }
        .sumOf { (it.timer.durationEpochMs * it.timer.progress).toLong() }
        .toMinutes()
    val totalBreakMinutes = elapsedSegments
        .filter { it.type != TimerType.FOCUS }
        .sumOf { (it.timer.durationEpochMs * it.timer.progress).toLong() }
        .toMinutes()

    return HistorySessionEntity(
        id = startedAtEpochMs,
        dateStartedEpochMs = startedAtEpochMs,
        totalFocusMinutes = totalFocusMinutes,
        totalBreakMinutes = totalBreakMinutes,
    )
}

fun List<HistorySessionEntity>.mapToDomain(timeZone: TimeZone): List<HistoryDomain> =
    map { it.toDomain(timeZone) }

@OptIn(ExperimentalTime::class)
private fun HistorySessionEntity.toDomain(timeZone: TimeZone): HistoryDomain {
    val date =
        Instant.fromEpochMilliseconds(dateStartedEpochMs).toLocalDateTime(timeZone).date.toString()
    return HistoryDomain(
        date = date,
        focusMinutes = totalFocusMinutes,
        breakMinutes = totalBreakMinutes,
    )
}
