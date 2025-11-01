package com.fakhry.pomodojo.utils

import com.fakhry.pomodojo.dashboard.model.ContributionCell
import com.fakhry.pomodojo.dashboard.model.intensityLevelForMinutes
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import kotlin.collections.chunked
import kotlin.collections.firstOrNull

fun formatTimerMinutes(minutes: Int) =
    "${minutes.coerceAtLeast(0).toString().padStart(2, '0')}:00"

fun ensureYearCells(
    selectedYear: Int,
    rawCells: List<ContributionCell>,
): List<ContributionCell> {
    if (rawCells.isEmpty()) return generateEmptyYear(selectedYear)
    val byDate = rawCells.associateBy { it.date }
    val start = LocalDate(selectedYear, 1, 1)
    val end = LocalDate(selectedYear, 12, 31)
    val result = mutableListOf<ContributionCell>()
    var cursor = start
    while (cursor <= end) {
        val key = cursor.toString()
        val cell = byDate[key]
        if (cell != null) {
            result += cell.copy(intensityLevel = intensityLevelForMinutes(cell.totalMinutes))
        } else {
            result += ContributionCell(
                date = key,
                totalMinutes = 0,
                intensityLevel = 0,
            )
        }
        cursor = cursor.plus(DatePeriod(days = 1))
    }
    return result
}

fun generateEmptyYear(year: Int): List<ContributionCell> {
    val start = LocalDate(year, 1, 1)
    val end = LocalDate(year, 12, 31)
    val result = mutableListOf<ContributionCell>()
    var cursor = start
    while (cursor <= end) {
        val key = cursor.toString()
        result += ContributionCell(
            date = key,
            totalMinutes = 0,
            intensityLevel = 0,
        )
        cursor = cursor.plus(DatePeriod(days = 1))
    }
    return result
}

fun formatCellDescription(cell: ContributionCell): String {
    val localDate = runCatching { LocalDate.parse(cell.date) }.getOrNull()
    val baseDate = localDate?.let { "${monthDisplayName(it.month)} ${it.day}, ${it.year}" }
        ?: cell.date
    val minuteDescription = if (cell.totalMinutes == 1) {
        "1 minute of focus"
    } else {
        "${cell.totalMinutes} minutes of focus"
    }
    return "$baseDate: $minuteDescription"
}

fun monthDisplayName(month: Month): String = when (month) {
    Month.JANUARY -> "January"
    Month.FEBRUARY -> "February"
    Month.MARCH -> "March"
    Month.APRIL -> "April"
    Month.MAY -> "May"
    Month.JUNE -> "June"
    Month.JULY -> "July"
    Month.AUGUST -> "August"
    Month.SEPTEMBER -> "September"
    Month.OCTOBER -> "October"
    Month.NOVEMBER -> "November"
    Month.DECEMBER -> "December"
}

fun monthShortName(month: Month): String = when (month) {
    Month.JANUARY -> "Jan"
    Month.FEBRUARY -> "Feb"
    Month.MARCH -> "Mar"
    Month.APRIL -> "Apr"
    Month.MAY -> "May"
    Month.JUNE -> "Jun"
    Month.JULY -> "Jul"
    Month.AUGUST -> "Aug"
    Month.SEPTEMBER -> "Sep"
    Month.OCTOBER -> "Oct"
    Month.NOVEMBER -> "Nov"
    Month.DECEMBER -> "Dec"
}

fun extractMonthLabels(cells: List<ContributionCell>): List<String> {
    if (cells.isEmpty()) return emptyList()

    // Group cells into weeks (7 days each)
    val weeks = cells.chunked(7)
    val monthLabels = mutableListOf<String>()

    var currentMonth: Month? = null
    weeks.forEach { week ->
        // Get the first cell of the week that has a valid date
        val firstDate = week.firstOrNull()?.date?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }

        if (firstDate != null) {
            // Show month label only when the month changes
            if (currentMonth != firstDate.month) {
                currentMonth = firstDate.month
                monthLabels.add(monthShortName(firstDate.month))
            } else {
                monthLabels.add("") // Empty label for weeks in the same month
            }
        } else {
            monthLabels.add("")
        }
    }

    return monthLabels
}