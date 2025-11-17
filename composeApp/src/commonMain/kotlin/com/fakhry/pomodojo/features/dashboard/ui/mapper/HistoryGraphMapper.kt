package com.fakhry.pomodojo.features.dashboard.ui.mapper

import com.fakhry.pomodojo.core.framework.formatting.platformThousandsSeparator
import com.fakhry.pomodojo.features.dashboard.domain.model.HistoryDomain
import com.fakhry.pomodojo.features.dashboard.domain.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.features.dashboard.ui.model.HistoryCell
import com.fakhry.pomodojo.features.dashboard.ui.model.HistorySectionUi
import com.fakhry.pomodojo.features.dashboard.ui.model.intensityLevelForMinutes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlin.math.absoluteValue

fun PomodoroHistoryDomain.mapToHistorySectionUi(
    selectedYear: Int,
    currentDate: LocalDate? = null,
): HistorySectionUi {
    val parsedEntries = histories.mapNotNull { it.toHistoryEntryOrNull() }.sortedBy { it.date }
    val historyCells = buildHistoryCells(parsedEntries, selectedYear, currentDate)

    return HistorySectionUi(
        focusMinutesThisYear = formatNumberWithSeparator(focusMinutesThisYear),
        selectedYear = selectedYear,
        availableYears = availableYears.toPersistentList(),
        cells = historyCells,
    )
}

private fun buildHistoryCells(
    entries: List<HistoryEntry>,
    year: Int,
    currentDate: LocalDate?,
): ImmutableList<ImmutableList<HistoryCell>> {
    val rows = mutableListOf<ImmutableList<HistoryCell>>()
    rows += dayLabelRow()

    val entriesByDate = entries.associateBy { it.date }
    val orderedDates = buildOrderedDates(year, currentDate)
    if (orderedDates.isEmpty()) {
        return rows.toPersistentList()
    }
    val startPadding =
        orderedDates.first().dayOfWeek.isoDayNumber.let { iso ->
            (iso - 1).coerceAtLeast(0)
        }
    val paddedDates =
        buildList {
            repeat(startPadding) { add(null) }
            addAll(orderedDates)
            val remainder = size % 7
            if (remainder != 0) {
                repeat(7 - remainder) { add(null) }
            }
        }

    val weeklyCells = paddedDates.chunked(7)
    weeklyCells.asReversed().forEach { week ->
        rows += buildWeekRow(week, entriesByDate)
    }

    return rows.toPersistentList()
}

private fun buildWeekRow(
    week: List<LocalDate?>,
    entriesByDate: Map<LocalDate, HistoryEntry>,
): ImmutableList<HistoryCell> {
    val monthLabel =
        week.firstNotNullOfOrNull { date ->
            date?.takeIf { it.day == 1 }?.month?.toLabel()
        }
    val dayCells =
        week.map { date ->
            when (date) {
                null -> HistoryCell.Empty
                else -> entriesByDate[date]?.toGraphCell() ?: date.toEmptyGraphCell()
            }
        }
    return buildList {
        add(monthLabel?.let(HistoryCell::Text) ?: HistoryCell.Empty)
        addAll(dayCells)
    }.toPersistentList()
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

private data class HistoryEntry(val date: LocalDate, val focusMinutes: Int, val breakMinutes: Int)

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

private fun LocalDate.toEmptyGraphCell(): HistoryCell.GraphLevel = HistoryCell.GraphLevel(
    intensityLevel = 0,
    focusMinutes = 0,
    breakMinutes = 0,
    date = formatToDdMmm(),
)

private fun LocalDate.formatToDdMmm(): String = "$day ${month.toLabel()}"

private fun buildOrderedDates(year: Int, currentDate: LocalDate?): List<LocalDate> {
    val start = LocalDate(year, Month.JANUARY, 1)
    val yearEnd = LocalDate(year, Month.DECEMBER, 31)
    val lastDate = currentDate?.takeIf { it.year == year }?.coerceAtMost(yearEnd) ?: yearEnd
    if (lastDate < start) {
        return emptyList()
    }

    val dates = mutableListOf<LocalDate>()
    var cursor = start
    while (cursor <= lastDate) {
        dates += cursor
        cursor = cursor.plus(DatePeriod(days = 1))
    }
    return dates
}

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
            isoFirst ->
                runCatching {
                    LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                }.getOrNull()?.let { return it }

            isoLast ->
                runCatching {
                    LocalDate(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                }.getOrNull()?.let { return it }
        }
    }

    return null
}

private fun Month.toLabel(): String = name.take(3).lowercase().replaceFirstChar { it.uppercase() }

/**
 * Formats a number with locale-appropriate thousands separators.
 * Uses comma (,) for most locales and period (.) for European locales.
 */
private fun formatNumberWithSeparator(number: Int): String {
    val separator = runCatching { platformThousandsSeparator() }.getOrDefault(',')
    val digits = number.toLong().absoluteValue.toString()
    if (digits.length <= 3) {
        return if (number < 0) "-$digits" else digits
    }

    val groupedDigits = digits.reversed().chunked(3).joinToString(separator.toString()).reversed()
    return if (number < 0) "-$groupedDigits" else groupedDigits
}
