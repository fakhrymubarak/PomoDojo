package com.fakhry.pomodojo.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.fakhry.pomodojo.dashboard.model.DashboardState
import com.fakhry.pomodojo.dashboard.model.contributionColorMap
import com.fakhry.pomodojo.dashboard.model.intensityLevelForMinutes
import com.fakhry.pomodojo.dashboard.model.previewDashboardState
import com.fakhry.pomodojo.ui.theme.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DashboardScreen(
    state: DashboardState,
    modifier: Modifier = Modifier,
    onStartPomodoro: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectYear: (Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize(),
        ) {
            WavyHeader(onOpenSettings = onOpenSettings)
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TimerCard(timerMinutes = state.timerMinutes, onStartPomodoro = onStartPomodoro)
                StatisticsCard(totalMinutes = state.focusMinutesThisYear)
                YearFilterRow(
                    years = state.availableYears,
                    selectedYear = state.selectedYear,
                    onSelectYear = onSelectYear,
                )
                ContributionCard(
                    selectedYear = state.selectedYear,
                    cells = remember(state.selectedYear, state.cells) {
                        ensureYearCells(state.selectedYear, state.cells)
                    },
                )
            }
        }
    }
}

@Composable
private fun WavyHeader(
    onOpenSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SecondaryGreen)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "PomoDojo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                ),
                modifier = Modifier.semantics { contentDescription = "PomoDojo Dashboard" },
            )
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.semantics { contentDescription = "Open settings" },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = TextWhite,
                )
            }
        }
    }
}

@Composable
private fun TimerCard(
    timerMinutes: Int,
    onStartPomodoro: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Timer with decorative dots
        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Decorative dots scattered around
            DecorativeDots()

            // Circular ring
            Canvas(
                modifier = Modifier.size(220.dp)
            ) {
                drawCircle(
                    color = DarkCircleBackground,
                    radius = size.minDimension / 2,
                )
                drawCircle(
                    color = Primary,
                    radius = size.minDimension / 2,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                )
            }

            // Timer text
            Text(
                text = formatTimerMinutes(timerMinutes),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                ),
                modifier = Modifier.semantics {
                    contentDescription = "Timer set to $timerMinutes minutes"
                },
                textAlign = TextAlign.Center,
            )
        }

        // Start button
        Button(
            onClick = onStartPomodoro,
            modifier = Modifier.semantics { contentDescription = "Start Pomodoro" },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = ButtonPrimary,
                contentColor = TextWhite,
            ),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text(
                text = "Start Pomodoro",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun DecorativeDots() {
    val dots = remember {
        listOf(
            Pair(0.15f, 0.15f) to Primary,
            Pair(0.25f, 0.85f) to SecondaryGreen,
            Pair(0.85f, 0.25f) to SecondaryGreen,
            Pair(0.75f, 0.75f) to Primary,
            Pair(0.1f, 0.5f) to Primary,
            Pair(0.9f, 0.5f) to SecondaryGreen,
            Pair(0.5f, 0.1f) to SecondaryGreen,
            Pair(0.5f, 0.9f) to Primary,
            Pair(0.2f, 0.35f) to SecondaryGreen,
            Pair(0.8f, 0.65f) to Primary,
            Pair(0.35f, 0.2f) to Primary,
            Pair(0.65f, 0.8f) to SecondaryGreen,
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        dots.forEach { (position, color) ->
            drawCircle(
                color = color,
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(
                    x = size.width * position.first,
                    y = size.height * position.second
                )
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
private fun YearFilterRow(
    years: List<Int>,
    selectedYear: Int,
    onSelectYear: (Int) -> Unit,
) {
    YearFilter(
        years = years,
        selectedYear = selectedYear,
        onSelectYear = onSelectYear,
    )
}

@Composable
private fun YearFilter(
    years: List<Int>,
    selectedYear: Int,
    onSelectYear: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.fillMaxWidth(),
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
private fun ContributionCard(
    selectedYear: Int,
    cells: List<ContributionCell>,
) {
    val semanticDescription = remember(selectedYear) {
        "Focus activity contribution graph for $selectedYear"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Image
                contentDescription = semanticDescription
            },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Day labels (Mon, Thu, Fri, Sun shown in design)
        androidx.compose.foundation.layout.Row(
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
        androidx.compose.foundation.layout.Row(
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
                    ContributionCellItem(cell = cell)
                }
            }
        }
    }
}

@Composable
private fun ContributionCellItem(cell: ContributionCell) {
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

private fun ensureYearCells(
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

private fun generateEmptyYear(year: Int): List<ContributionCell> {
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

private fun formatTimerMinutes(minutes: Int): String = "${minutes.coerceAtLeast(0).toString().padStart(2, '0')}:00"

private fun formatCellDescription(cell: ContributionCell): String {
    val localDate = runCatching { LocalDate.parse(cell.date) }.getOrNull()
    val baseDate = localDate?.let { "${monthDisplayName(it.month)} ${it.dayOfMonth}, ${it.year}" }
        ?: cell.date
    val minuteDescription = if (cell.totalMinutes == 1) {
        "1 minute of focus"
    } else {
        "${cell.totalMinutes} minutes of focus"
    }
    return "$baseDate: $minuteDescription"
}

private fun monthDisplayName(month: Month): String = when (month) {
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

private fun monthShortName(month: Month): String = when (month) {
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

private fun extractMonthLabels(cells: List<ContributionCell>): List<String> {
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

@Preview
@Composable
private fun DashboardPreview() {
    val previewState = previewDashboardState.copy(
        cells = ensureYearCells(
            previewDashboardState.selectedYear,
            generatePreviewCells(previewDashboardState.selectedYear),
        ),
    )
    PomoDojoTheme {
        DashboardScreen(
            state = previewState,
            onStartPomodoro = {},
            onOpenSettings = {},
            onSelectYear = {},
        )
    }
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
