package com.fakhry.pomodojo.dashboard.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakhry.pomodojo.dashboard.model.ContributionCell
import com.fakhry.pomodojo.dashboard.model.contributionColorMap
import com.fakhry.pomodojo.dashboard.model.intensityLevelForMinutes
import com.fakhry.pomodojo.dashboard.model.previewDashboardState
import com.fakhry.pomodojo.ui.theme.ButtonSecondary
import com.fakhry.pomodojo.ui.theme.GraphLevel0
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.ui.theme.SecondaryGreen
import com.fakhry.pomodojo.ui.theme.TextLightGray
import com.fakhry.pomodojo.ui.theme.TextWhite
import com.fakhry.pomodojo.utils.ensureYearCells
import com.fakhry.pomodojo.utils.extractMonthLabels
import com.fakhry.pomodojo.utils.formatCellDescription
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Focus History Section
 * Contains statistics, year filter, and activity graph
 */
@Composable
fun FocusHistorySection(
    totalMinutes: Int,
    selectedYear: Int,
    availableYears: List<Int>,
    cells: List<ContributionCell>,
    onSelectYear: (Int) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Statistics
        StatisticsCard(totalMinutes = totalMinutes)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Focus History Graph
            FocusHistoryGraph(
                modifier = Modifier.weight(1f),
                selectedYear = selectedYear,
                cells = remember(selectedYear, cells) {
                    ensureYearCells(selectedYear, cells)
                },
            )
            // Year Filter
            YearFilters(
                years = availableYears,
                selectedYear = selectedYear,
                onSelectYear = onSelectYear,
            )
        }
    }
}


@Composable
private fun StatisticsCard(totalMinutes: Int) {
    Text(
        text = "$totalMinutes minutes of focus in the last year",
        style = MaterialTheme.typography.bodyMedium.copy(
            color = TextLightGray,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$totalMinutes minutes of focus this year"
            },
    )
}

@Composable
private fun YearFilters(
    years: List<Int>,
    selectedYear: Int,
    onSelectYear: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        years.forEach { year ->
            Box(
                modifier = Modifier
                    .background(
                        color = if (year == selectedYear) SecondaryGreen else ButtonSecondary,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectYear(year) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = if (year == selectedYear) {
                            "Viewing activity for $year"
                        } else {
                            "Switch to activity from $year"
                        }
                    }
            ) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                        color = if (year == selectedYear) TextWhite else TextLightGray,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FocusHistoryGraph(
    modifier: Modifier,
    selectedYear: Int,
    cells: List<ContributionCell>,
) {
    val semanticDescription = remember(selectedYear) {
        "Focus history activity graph for $selectedYear"
    }

    Column(
        modifier = modifier
            .semantics {
                role = Role.Image
                contentDescription = semanticDescription
            },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf("Mon", "", "Thu", "", "Fri", "", "Sun").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextLightGray,
                        fontSize = 8.sp,
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Graph with month labels and cells
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Month labels on the left
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(32.dp),
            ) {
                // Add month labels for each week row
                val monthsToShow = remember(cells) {
                    extractMonthLabels(cells)
                }
                monthsToShow.forEach { monthLabel ->
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextLightGray,
                            fontSize = 8.sp,
                        ),
                        modifier = Modifier.height(16.dp),
                    )
                }
            }

            // Grid of cells
            val cellSize = 12.dp
            val cellSpacing = 4.dp
            val rows = remember(cells.size) { (cells.size + 6) / 7 } // Calculate number of rows
            val gridHeight = remember(rows) {
                cellSize * rows + cellSpacing * (rows - 1)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(cellSpacing),
                verticalArrangement = Arrangement.spacedBy(cellSpacing),
                modifier = Modifier
                    .weight(1f)
                    .height(gridHeight),
                contentPadding = PaddingValues(0.dp),
                userScrollEnabled = false,
            ) {
                items(cells, key = { it.date }) { cell ->
                    FocusHistoryCellItem(cell = cell)
                }
            }
        }
    }
}

@Composable
private fun FocusHistoryCellItem(cell: ContributionCell) {
    val color = contributionColorMap[cell.intensityLevel]?.let { Color(it) }
        ?: GraphLevel0
    val textDescription = remember(cell) { formatCellDescription(cell) }
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(
                color = color,
                shape = RoundedCornerShape(2.dp)
            )
            .focusable()
            .semantics { contentDescription = textDescription },
    )
}

private fun generatePreviewCells(year: Int): List<ContributionCell> {
    val start = LocalDate(year, 1, 1)
    val result = mutableListOf<ContributionCell>()
    var cursor = start
    var bucket = 0
    while (bucket < 35) { // generate enough days for preview variety
        val totalMinutes = when (bucket % 6) {
            0 -> 0
            1 -> 15
            2 -> 25
            3 -> 50
            4 -> 75
            else -> 85
        }
        result += ContributionCell(
            date = cursor.toString(),
            totalMinutes = totalMinutes,
            intensityLevel = intensityLevelForMinutes(totalMinutes),
        )
        cursor = cursor.plus(DatePeriod(days = 1))
        bucket += 1
    }
    return result
}


@Preview
@Composable
fun FocusHistorySectionPreview() {

    val previewState = previewDashboardState.copy(
        cells = ensureYearCells(
            previewDashboardState.selectedYear,
            generatePreviewCells(previewDashboardState.selectedYear),
        ),
    )
    PomoDojoTheme {
        FocusHistorySection(
            totalMinutes = previewState.focusMinutesThisYear,
            selectedYear = previewState.selectedYear,
            availableYears = previewState.availableYears,
            cells = previewState.cells,

            )
    }
}