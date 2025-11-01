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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.dashboard.model.HistoryCell
import com.fakhry.pomodojo.dashboard.model.contributionColorMap
import com.fakhry.pomodojo.dashboard.model.previewDashboardState
import com.fakhry.pomodojo.ui.theme.ButtonSecondary
import com.fakhry.pomodojo.ui.theme.GraphLevel0
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import com.fakhry.pomodojo.ui.theme.Secondary
import com.fakhry.pomodojo.ui.theme.TextLightGray
import com.fakhry.pomodojo.ui.theme.TextWhite
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
    cells: List<List<HistoryCell>>,
    onSelectYear: (Int) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatisticsCard(totalMinutes = totalMinutes)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            // Focus History Graph
            FocusHistoryGraph(
                modifier = Modifier.weight(1f),
                selectedYear = selectedYear,
                cells = remember(selectedYear, cells) {
                    cells
                },
            )
            Spacer(modifier = Modifier.width(16.dp))
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
        modifier = Modifier.fillMaxWidth().semantics {
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
                modifier = Modifier.background(
                    color = if (year == selectedYear) Secondary else ButtonSecondary,
                    shape = RoundedCornerShape(16.dp)
                ).clickable { onSelectYear(year) }.padding(horizontal = 16.dp, vertical = 8.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = if (year == selectedYear) {
                            "Viewing activity for $year"
                        } else {
                            "Switch to activity from $year"
                        }
                    }) {
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
    modifier: Modifier = Modifier,
    selectedYear: Int,
    cells: List<List<HistoryCell>>,
) {
    val semanticDescription = remember(selectedYear) {
        "Focus history activity graph for $selectedYear"
    }

    val columns = 8
    val cellSize = 24.dp
    val cellSpacing = 8.dp
    val flattenedCells = remember(cells) { cells.flatten() }
    val rows = remember(flattenedCells.size) {
        if (flattenedCells.isEmpty()) 0 else (flattenedCells.size + columns - 1) / columns
    }
    val gridWidth = cellSize * columns + cellSpacing * (columns - 1)
    val gridHeight = if (rows <= 0) 0.dp else cellSize * rows + cellSpacing * (rows - 1)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(cellSpacing),
        verticalArrangement = Arrangement.spacedBy(cellSpacing),
        modifier = modifier
            .width(gridWidth)
            .height(gridHeight)
            .semantics {
                role = Role.Image
                contentDescription = semanticDescription
            },
        contentPadding = PaddingValues(0.dp),
        userScrollEnabled = false,
    ) {
        items(flattenedCells.size) { index ->
            FocusHistoryCellItem(
                cell = flattenedCells[index],
                cellSize = cellSize,
            )
        }
    }
}

@Composable
private fun FocusHistoryCellItem(
    cell: HistoryCell,
    cellSize: Dp,
) {
    when (cell) {
        HistoryCell.Empty -> {
            Box(modifier = Modifier.size(cellSize))
        }

        is HistoryCell.Text -> {
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .semantics { contentDescription = cell.text },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = cell.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextLightGray,
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }

        is HistoryCell.GraphLevel -> {
            val color = contributionColorMap[cell.intensityLevel] ?: GraphLevel0
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(2.dp),
                    )
                    .focusable()
            )
        }
    }
}

@Preview
@Composable
fun FocusHistorySectionPreview() {
    val previewState = previewDashboardState
    PomoDojoTheme {
        FocusHistorySection(
            totalMinutes = previewState.focusMinutesThisYear,
            selectedYear = previewState.selectedYear,
            availableYears = previewState.availableYears,
            cells = previewState.cells,
        )
    }
}
