package com.fakhry.pomodojo.dashboard.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.fakhry.pomodojo.dashboard.model.HistoryCell
import com.fakhry.pomodojo.dashboard.model.contributionColorMap
import com.fakhry.pomodojo.dashboard.model.previewDashboardState
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.focus_history_cell_tooltip
import com.fakhry.pomodojo.generated.resources.focus_history_graph_content_description
import com.fakhry.pomodojo.generated.resources.focus_history_selected_year_description
import com.fakhry.pomodojo.generated.resources.focus_history_switch_year_description
import com.fakhry.pomodojo.generated.resources.focus_history_total_minutes
import com.fakhry.pomodojo.ui.theme.GraphLevel0
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val TooltipVerticalSpacing = 8.dp

/**
 * Focus History Section
 * Contains statistics, year filter, and activity graph
 */
@Composable
fun FocusHistorySection(
    modifier: Modifier = Modifier,
    totalMinutes: Int,
    selectedYear: Int,
    availableYears: ImmutableList<Int>,
    cells: ImmutableList<ImmutableList<HistoryCell>>,
    onSelectYear: (Int) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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
                cells =
                    remember(selectedYear, cells) {
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
    val totalMinutesText = stringResource(Res.string.focus_history_total_minutes, totalMinutes)
    Text(
        text = totalMinutesText,
        style =
            MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = totalMinutesText
                },
    )
}

@Composable
private fun YearFilters(
    years: ImmutableList<Int>,
    selectedYear: Int,
    onSelectYear: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        years.forEach { year ->
            val selectedDescription =
                stringResource(Res.string.focus_history_selected_year_description, year)
            val switchDescription =
                stringResource(Res.string.focus_history_switch_year_description, year)
            val isSelected = year == selectedYear
            Box(
                modifier =
                    Modifier.background(
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        shape = RoundedCornerShape(16.dp),
                    ).clickable { onSelectYear(year) }.padding(horizontal = 16.dp, vertical = 8.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription =
                                if (isSelected) {
                                    selectedDescription
                                } else {
                                    switchDescription
                                }
                        },
            ) {
                Text(
                    text = year.toString(),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        ),
                )
            }
        }
    }
}

@Composable
private fun FocusHistoryGraph(
    modifier: Modifier = Modifier,
    selectedYear: Int,
    cells: ImmutableList<ImmutableList<HistoryCell>>,
) {
    val semanticDescription =
        stringResource(Res.string.focus_history_graph_content_description, selectedYear)

    val columns = 8
    val cellSize = 24.dp
    val cellSpacing = 8.dp
    val flattenedCells = remember(cells) { cells.flatten() }
    val rows =
        remember(flattenedCells.size) {
            if (flattenedCells.isEmpty()) 0 else (flattenedCells.size + columns - 1) / columns
        }
    val gridWidth = cellSize * columns + cellSpacing * (columns - 1)
    val gridHeight = if (rows <= 0) 0.dp else cellSize * rows + cellSpacing * (rows - 1)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(cellSpacing),
        verticalArrangement = Arrangement.spacedBy(cellSpacing),
        modifier =
            modifier
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FocusHistoryCellItem(
    cell: HistoryCell,
    cellSize: Dp,
) {
    val hapticFeedback = LocalHapticFeedback.current

    when (cell) {
        HistoryCell.Empty -> {
            Box(modifier = Modifier.size(cellSize))
        }

        is HistoryCell.Text -> {
            Box(
                modifier =
                    Modifier
                        .size(cellSize)
                        .semantics { contentDescription = cell.text },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = cell.text,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }

        is HistoryCell.GraphLevel -> {
            val color = contributionColorMap[cell.intensityLevel] ?: GraphLevel0
            val tooltipText =
                stringResource(
                    Res.string.focus_history_cell_tooltip,
                    cell.focusMinutes,
                    cell.breakMinutes,
                )
            var showTooltip by remember(cell.focusMinutes, cell.breakMinutes) {
                mutableStateOf(false)
            }

            // Hide the tooltip automatically so it does not stick around indefinitely.
            LaunchedEffect(showTooltip) {
                if (showTooltip) {
                    delay(2000)
                    showTooltip = false
                }
            }

            Box(
                modifier =
                    Modifier
                        .size(cellSize)
                        .combinedClickable(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                                showTooltip = true
                            },
                        )
                        .semantics { contentDescription = tooltipText }
                        .focusable(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                color = color,
                                shape = RoundedCornerShape(2.dp),
                            ),
                )

                if (showTooltip) {
                    val density = LocalDensity.current
                    val positionProvider =
                        remember(density) {
                            TooltipPositionProvider(density)
                        }

                    Popup(
                        popupPositionProvider = positionProvider,
                        onDismissRequest = { showTooltip = false },
                        properties =
                            PopupProperties(
                                focusable = false,
                                dismissOnBackPress = false,
                                dismissOnClickOutside = true,
                            ),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = tooltipText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private class TooltipPositionProvider(
    private val density: Density,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val spacing = with(density) { TooltipVerticalSpacing.roundToPx() }
        val anchorStart =
            when (layoutDirection) {
                LayoutDirection.Ltr -> anchorBounds.left
                LayoutDirection.Rtl -> anchorBounds.right - anchorBounds.width
            }
        val preferredX = anchorStart + (anchorBounds.width - popupContentSize.width) / 2
        val maxX = windowSize.width - popupContentSize.width
        val resolvedX =
            when {
                maxX <= 0 -> 0
                else -> preferredX.coerceIn(0, maxX)
            }

        val preferredY = anchorBounds.top - popupContentSize.height - spacing
        val fallbackY = anchorBounds.bottom + spacing
        val candidateY = if (preferredY >= 0) preferredY else fallbackY
        val maxY = windowSize.height - popupContentSize.height
        val resolvedY =
            when {
                maxY <= 0 -> candidateY.coerceAtLeast(0)
                else -> candidateY.coerceIn(0, maxY)
            }

        return IntOffset(resolvedX, resolvedY)
    }
}

@Preview
@Composable
fun FocusHistorySectionPreview() {
    val previewState = previewDashboardState
    PomoDojoTheme {
        FocusHistorySection(
            totalMinutes = previewState.historySection.focusMinutesThisYear,
            selectedYear = previewState.historySection.selectedYear,
            availableYears = previewState.historySection.availableYears,
            cells = previewState.historySection.cells,
        )
    }
}
