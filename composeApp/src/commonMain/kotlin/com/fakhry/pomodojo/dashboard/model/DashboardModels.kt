package com.fakhry.pomodojo.dashboard.model

/**
 * Dashboard level state that the UI consumes.
 */
data class DashboardState(
    val timerMinutes: Int,
    val focusMinutesThisYear: Int,
    val selectedYear: Int,
    val availableYears: List<Int>,
    val cells: List<ContributionCell>,
)

/**
 * Contribution cell representing one day in the activity grid.
 */
data class ContributionCell(
    val date: String, // ISO-8601 formatted date (YYYY-MM-DD)
    val totalMinutes: Int,
    val intensityLevel: Int,
)

/**
 * Snapshot used for previews and when backend data is not yet wired.
 */
val previewDashboardState = DashboardState(
    timerMinutes = 25,
    focusMinutesThisYear = 512,
    selectedYear = 2025,
    availableYears = listOf(2025, 2024, 2023),
    cells = listOf(
        ContributionCell("2025-01-01", 0, 0),
        ContributionCell("2025-01-02", 15, 1),
        ContributionCell("2025-01-03", 25, 2),
        ContributionCell("2025-01-04", 50, 3),
        ContributionCell("2025-01-05", 75, 4),
        ContributionCell("2025-01-06", 85, 6),
    ),
)

/**
 * Mapping from intensity level to color hex encoded in the specification.
 */
val contributionColorMap = mapOf(
    0 to 0xFFD9D9D9,
    1 to 0xFF7BB35D,
    2 to 0xFF6FA054,
    3 to 0xFF638E49,
    4 to 0xFF577D41,
    5 to 0xFF496B38,
    6 to 0xFF3E5A2F,
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
