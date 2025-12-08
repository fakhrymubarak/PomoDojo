package com.fakhry.pomodojo.tools
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Test data generator for history session rows used in SQL fixtures.
 */
object TestDataGenerator {

    data class HistorySessionRow(
        val id: Long,
        val dateStartedEpochMs: Long,
        val totalFocusMinutes: Int,
        val totalBreakMinutes: Int,
    )

    /**
     * Generates [count] days of history session test data spanning 2024-2025
     *
     * @param count Number of days to generate (default: 600 - covers 2024 and most of 2025)
     * @param startFromToday If true, starts from today going back. If false, starts from Feb 15, 2024
     * @param minFocusMinutes Minimum focus minutes per day (default: 25)
     * @param maxFocusMinutes Maximum focus minutes per day (default: 300)
     * @param timezone Timezone to use for date calculations (default: UTC)
     * @param seed Random seed for reproducible results (default: null for random)
     *
     * @return List of HistorySessionEntity with random focus/break times
     */
    @OptIn(ExperimentalTime::class)
    fun generateHistoryData(
        count: Int = 600,
        startFromToday: Boolean = false,
        minFocusMinutes: Int = 25,
        maxFocusMinutes: Int = 300,
        timezone: TimeZone = TimeZone.Companion.UTC,
        seed: Int? = null,
    ): List<HistorySessionRow> {
        val random = seed?.let { Random(it) } ?: Random.Default

        val startDate = if (startFromToday) {
            val today = Clock.System.now().toLocalDateTime(timezone).date
            today.minus(DatePeriod(days = count - 1))
        } else {
            // Default: Start from Feb 15, 2024 to cover 2024-2025
            LocalDate(2024, 2, 15)
        }

        return (0 until count).map { dayOffset ->
            val currentDate = startDate.plus(DatePeriod(days = dayOffset))
            val dateStartedEpochMs = currentDate.atStartOfDayIn(timezone).toEpochMilliseconds()

            // Random focus minutes
            val totalFocusMinutes = random.nextInt(minFocusMinutes, maxFocusMinutes + 1)
            // Break is half of focus (rounded down)
            val totalBreakMinutes = totalFocusMinutes / 2

            HistorySessionRow(
                id = (dayOffset + 1).toLong(),
                dateStartedEpochMs = dateStartedEpochMs,
                totalFocusMinutes = totalFocusMinutes,
                totalBreakMinutes = totalBreakMinutes,
            )
        }
    }

    /**
     * Generates history data for a specific date range
     */
    @OptIn(ExperimentalTime::class)
    fun generateHistoryDataForDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        minFocusMinutes: Int = 25,
        maxFocusMinutes: Int = 300,
        timezone: TimeZone = TimeZone.Companion.UTC,
        seed: Int? = null,
    ): List<HistorySessionRow> {
        val random = seed?.let { Random(it) } ?: Random.Default

        val results = mutableListOf<HistorySessionRow>()
        var currentDate = startDate
        var id = 1L

        while (currentDate <= endDate) {
            val dateStartedEpochMs = currentDate.atStartOfDayIn(timezone).toEpochMilliseconds()
            val totalFocusMinutes = random.nextInt(minFocusMinutes, maxFocusMinutes + 1)
            val totalBreakMinutes = totalFocusMinutes / 2

            results.add(
                HistorySessionRow(
                    id = id++,
                    dateStartedEpochMs = dateStartedEpochMs,
                    totalFocusMinutes = totalFocusMinutes,
                    totalBreakMinutes = totalBreakMinutes,
                ),
            )

            currentDate = currentDate.plus(DatePeriod(days = 1))
        }

        return results
    }

    /**
     * Generates SQL INSERT statements for the generated data
     */
    fun generateSQLInserts(data: List<HistorySessionRow>): String {
        val header =
            "INSERT INTO history_sessions (id, date_started, total_focus_minutes, total_break_minutes) VALUES\n"
        val values = data.joinToString(",\n") { entity ->
            "(${entity.id}, ${entity.dateStartedEpochMs}, ${entity.totalFocusMinutes}, ${entity.totalBreakMinutes})"
        }
        return "$header$values;"
    }
}

/**
 * Example usage in tests:
 *
 * ```kotlin
 * @Test
 * fun `test with 600 days of history data spanning 2024-2025`() = runTest {
 *     val testData = TestDataGenerator.generateHistoryData(
 *         // Default is 600 days starting Feb 15, 2024
 *         seed = 12345 // for reproducible tests
 *     )
 *
 *     testData.forEach { entity ->
 *         historyDao.insertFinishedSession(entity)
 *     }
 *
 *     // Your test assertions here
 * }
 *
 * @Test
 * fun `test with custom date range`() = runTest {
 *     val testData = TestDataGenerator.generateHistoryData(
 *         count = 365, // Override to 365 days for one year
 *         startFromToday = true // Start from today going back
 *     )
 *     // ...
 * }
 * ```
 */
