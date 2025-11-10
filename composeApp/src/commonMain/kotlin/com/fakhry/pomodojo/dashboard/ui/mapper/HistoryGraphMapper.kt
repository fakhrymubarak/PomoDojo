package com.fakhry.pomodojo.dashboard.ui.mapper

import com.fakhry.pomodojo.dashboard.domain.model.HistoryDomain
import com.fakhry.pomodojo.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.dashboard.ui.model.HistoryCell
import com.fakhry.pomodojo.dashboard.ui.model.HistorySectionUi
import com.fakhry.pomodojo.dashboard.ui.model.intensityLevelForMinutes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

fun PomodoroHistoryDomain.mapToHistorySectionUi(
    selectedYear: Int,
): HistorySectionUi {
    val parsedEntries = histories.mapNotNull { it.toHistoryEntryOrNull() }.sortedBy { it.date }
    val historyCells = buildHistoryCells(parsedEntries, selectedYear)

    return HistorySectionUi(
        focusMinutesThisYear = focusMinutesThisYear,
        selectedYear = selectedYear,
        availableYears = persistentListOf(2023, 2024, 2025),
        cells = historyCells,
    )
}

private fun buildHistoryCells(
    entries: List<HistoryEntry>,
    year: Int,
): ImmutableList<ImmutableList<HistoryCell>> {
    val rows = mutableListOf<ImmutableList<HistoryCell>>()
    rows += dayLabelRow()

    val entriesByDate = entries.associateBy { it.date }

    Month.entries.toTypedArray().sortedByDescending { it.ordinal }.forEach { month ->
        val dailyCells = generateDatesForMonth(year, month).map { date ->
            entriesByDate[date]?.toGraphCell() ?: EmptyGraphCell
        }
        val weeklyCells = dailyCells.chunked(7)
        weeklyCells.forEachIndexed { index, chunk ->
            val paddedChunk = if (chunk.size < 7) {
                chunk + List(7 - chunk.size) { EmptyGraphCell }
            } else {
                chunk
            }
            val row = buildList {
                add(
                    if (index == 0) {
                        HistoryCell.Text(month.toLabel())
                    } else {
                        HistoryCell.Empty
                    },
                )
                addAll(paddedChunk)
            }.toPersistentList()
            rows += row
        }
    }

    return rows.toPersistentList()
}

private fun dayLabelRow(): ImmutableList<HistoryCell> = persistentListOf(
    HistoryCell.Empty,
    HistoryCell.Text("Mon"),
    HistoryCell.Empty,
    HistoryCell.Text("Wed"),
    HistoryCell.Empty,
    HistoryCell.Text("Fri"),
    HistoryCell.Empty,
    HistoryCell.Text("Sun"),
)

private data class HistoryEntry(
    val date: LocalDate,
    val focusMinutes: Int,
    val breakMinutes: Int,
)

private fun HistoryDomain.toHistoryEntryOrNull(): HistoryEntry? {
    val parsedDate = date.toLocalDateOrNull() ?: return null
    return HistoryEntry(
        date = parsedDate,
        focusMinutes = focusMinutes,
        breakMinutes = breakMinutes,
    )
}

private fun HistoryEntry.toGraphCell(): HistoryCell.GraphLevel = HistoryCell.GraphLevel(
    intensityLevel = intensityLevelForMinutes(focusMinutes),
    date = date.formatToDdMmm(),
    focusMinutes = focusMinutes,
    breakMinutes = breakMinutes,
)

private fun LocalDate.formatToDdMmm(): String {
    val date = this.day
    val month = this.month.toLabel()
    return "$date $month"
}

private fun generateDatesForMonth(
    year: Int,
    month: Month,
): List<LocalDate> {
    val daysInMonth = when (month) {
        Month.JANUARY,
        Month.MARCH,
        Month.MAY,
        Month.JULY,
        Month.AUGUST,
        Month.OCTOBER,
        Month.DECEMBER,
            -> 31

        Month.APRIL,
        Month.JUNE,
        Month.SEPTEMBER,
        Month.NOVEMBER,
            -> 30

        Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
    }
    return (daysInMonth downTo 1).map { day ->
        LocalDate(year = year, month = month.ordinal + 1, day = day)
    }
}

private fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

private fun String.toLocalDateOrNull(): LocalDate? {
    val normalized = trim()
    if (normalized.isEmpty()) return null

    runCatching { LocalDate.parse(normalized) }.getOrNull()?.let { return it }

    val separators = listOf("-", "/")
    separators.forEach { separator ->
        val parts = normalized.split(separator)
        if (parts.size != 3) return@forEach
        val isoFirst = parts[0].length == 4
        val isoLast = parts[2].length == 4
        when {
            isoFirst -> runCatching {
                LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            }.getOrNull()?.let { return it }

            isoLast -> runCatching {
                LocalDate(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
            }.getOrNull()?.let { return it }
        }
    }

    return null
}

private fun Month.toLabel(): String = name.take(3).lowercase().replaceFirstChar { it.uppercase() }

private val EmptyGraphCell = HistoryCell.GraphLevel(0, 0, 0)
