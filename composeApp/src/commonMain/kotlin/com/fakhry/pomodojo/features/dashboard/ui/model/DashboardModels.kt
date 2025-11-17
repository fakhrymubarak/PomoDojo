package com.fakhry.pomodojo.features.dashboard.ui.model

import com.fakhry.pomodojo.core.ui.theme.GraphLevel0
import com.fakhry.pomodojo.core.ui.theme.GraphLevel1
import com.fakhry.pomodojo.core.ui.theme.GraphLevel2
import com.fakhry.pomodojo.core.ui.theme.GraphLevel3
import com.fakhry.pomodojo.core.ui.theme.GraphLevel4
import com.fakhry.pomodojo.core.ui.theme.GraphLevel5
import com.fakhry.pomodojo.core.ui.theme.GraphLevel6
import kotlinx.collections.immutable.persistentListOf
import kotlin.random.Random

/**
 * Dashboard level state that the UI consumes.
 */
data class DashboardState(val timerMinutes: Int, val historySection: HistorySectionUi)

/**
 * Mapping from intensity level to color hex encoded in the specification.
 */
val contributionColorMap =
    mapOf(
        0 to GraphLevel0,
        1 to GraphLevel1,
        2 to GraphLevel2,
        3 to GraphLevel3,
        4 to GraphLevel4,
        5 to GraphLevel5,
        6 to GraphLevel6,
    )

/**
 * Utility to compute intensity level from total focus minutes.
 */
fun intensityLevelForMinutes(totalMinutes: Int): Int = when {
    totalMinutes <= 0 -> 0
    totalMinutes in 1..16 -> 1
    totalMinutes in 17..33 -> 2
    totalMinutes in 34..50 -> 3
    totalMinutes in 51..67 -> 4
    totalMinutes in 68..84 -> 5
    else -> 6
}

/**
 * Snapshot used for previews and when backend data is not yet wired.
 */
val previewDashboardState =
    DashboardState(
        timerMinutes = 25,
        historySection =
        HistorySectionUi(
            focusMinutesThisYear = "512",
            selectedYear = 2025,
            availableYears = persistentListOf(2025, 2024, 2023),
            cells =
            persistentListOf(
                persistentListOf(
                    HistoryCell.Empty,
                    HistoryCell.Text("Mon"),
                    HistoryCell.Empty,
                    HistoryCell.Text("Wed"),
                    HistoryCell.Empty,
                    HistoryCell.Text("Fri"),
                    HistoryCell.Empty,
                    HistoryCell.Text("Sun"),
                ),
                persistentListOf(
                    HistoryCell.Text("Nov"),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(5, 500, 50),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(6, 600, 60),
                    HistoryCell.GraphLevel(0, 0, 0),
                    HistoryCell.Empty,
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("Oct"),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(5, 500, 50),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("Sep"),
                    HistoryCell.GraphLevel(1, 100, 10),
                    HistoryCell.GraphLevel(0, 0, 0),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("Aug"),
                    HistoryCell.GraphLevel(5, 500, 50),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(5, 500, 50),
                    HistoryCell.GraphLevel(6, 600, 60),
                    HistoryCell.GraphLevel(5, 500, 50),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("Jul"),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(1, 100, 10),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(1, 100, 10),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("Jun"),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(5, 500, 50),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("May"),
                    HistoryCell.GraphLevel(1, 100, 10),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(1, 100, 10),
                    HistoryCell.GraphLevel(0, 0, 0),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("Apr"),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(5, 500, 50),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(1, 100, 10),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("Mar"),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(1, 100, 10),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("Feb"),
                    HistoryCell.GraphLevel(1, 100, 10),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(1, 100, 10),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                generateDummyWeek(),
                persistentListOf(
                    HistoryCell.Text("Jan"),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(4, 400, 40),
                    HistoryCell.GraphLevel(5, 500, 50),
                    HistoryCell.GraphLevel(3, 300, 30),
                    HistoryCell.GraphLevel(2, 200, 20),
                    HistoryCell.GraphLevel(0, 0, 0),
                ),
            ),
        ),
    )

fun generateDummyWeek() = persistentListOf(
    HistoryCell.Empty,
    Random.nextInt(0, 6).let { HistoryCell.GraphLevel(it, it * 100, it * 10) },
    Random.nextInt(0, 6).let { HistoryCell.GraphLevel(it, it * 100, it * 10) },
    Random.nextInt(0, 6).let { HistoryCell.GraphLevel(it, it * 100, it * 10) },
    Random.nextInt(0, 6).let { HistoryCell.GraphLevel(it, it * 100, it * 10) },
    Random.nextInt(0, 6).let { HistoryCell.GraphLevel(it, it * 100, it * 10) },
    Random.nextInt(0, 6).let { HistoryCell.GraphLevel(it, it * 100, it * 10) },
    Random.nextInt(0, 6).let { HistoryCell.GraphLevel(it, it * 100, it * 10) },
)
