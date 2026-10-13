package com.fakhry.pomodojo.data.history.mapper

import com.fakhry.pomodojo.core.database.entities.HistorySessionEntity
import com.fakhry.pomodojo.domain.history.model.HistoryDomain
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.completionSummary
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun PomodoroSessionDomain.toHistoryEntity(): HistorySessionEntity {
    val summary = timeline.completionSummary()
    return HistorySessionEntity(
        id = startedAtEpochMs,
        dateStartedEpochMs = startedAtEpochMs,
        totalFocusMinutes = summary.focusMinutes,
        totalBreakMinutes = summary.breakMinutes,
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
