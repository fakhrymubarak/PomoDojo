package com.fakhry.pomodojo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.fakhry.pomodojo.dashboard.model.ContributionCell
import com.fakhry.pomodojo.dashboard.DashboardScreen
import com.fakhry.pomodojo.dashboard.model.DashboardState
import com.fakhry.pomodojo.dashboard.model.intensityLevelForMinutes
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

@Composable
@Preview
fun App() {
    MaterialTheme {
        val sampleData = remember { buildSampleDashboardData() }
        var selectedYear by remember { mutableStateOf(sampleData.defaultYear) }
        val cells = sampleData.cellsForYear(selectedYear)
        val totalMinutes = cells.sumOf { it.totalMinutes }
        val state = DashboardState(
            timerMinutes = sampleData.timerMinutes,
            focusMinutesThisYear = totalMinutes,
            selectedYear = selectedYear,
            availableYears = sampleData.availableYears,
            cells = cells,
        )
        DashboardScreen(
            state = state,
            onStartPomodoro = { /* TODO: connect to timer screen */ },
            onOpenSettings = { /* TODO: navigate to preferences */ },
            onSelectYear = { selectedYear = it },
        )
    }
}

private data class SampleDashboardData(
    val timerMinutes: Int,
    private val dataByYear: Map<Int, List<ContributionCell>>,
) {
    val availableYears: List<Int> = dataByYear.keys.sortedDescending()
    val defaultYear: Int = availableYears.first()

    fun cellsForYear(year: Int): List<ContributionCell> = dataByYear[year] ?: emptyList()
}

private fun buildSampleDashboardData(): SampleDashboardData {
    val timerMinutes = 25
    val dataByYear = buildMap {
        put(2025, generateSampleCells(year = 2025) { index ->
            when (index % 14) {
                0 -> 75
                1, 2, 3 -> 50
                4, 5, 6 -> 25
                7, 8 -> 15
                else -> 0
            }
        })
        put(2024, generateSampleCells(year = 2024) { index ->
            when (index % 10) {
                0 -> 50
                1, 2, 3 -> 25
                4 -> 15
                else -> 0
            }
        })
        put(2023, emptyList())
    }
    return SampleDashboardData(
        timerMinutes = timerMinutes,
        dataByYear = dataByYear,
    )
}

private fun generateSampleCells(
    year: Int,
    minutesForDay: (Int) -> Int,
): List<ContributionCell> {
    val start = LocalDate(year, 1, 1)
    val end = LocalDate(year, 12, 31)
    val result = mutableListOf<ContributionCell>()
    var index = 0
    var cursor = start
    while (cursor <= end) {
        val totalMinutes = minutesForDay(index)
        result += ContributionCell(
            date = cursor.toString(),
            totalMinutes = totalMinutes,
            intensityLevel = intensityLevelForMinutes(totalMinutes),
        )
        cursor = cursor.plus(DatePeriod(days = 1))
        index += 1
    }
    return result
}
