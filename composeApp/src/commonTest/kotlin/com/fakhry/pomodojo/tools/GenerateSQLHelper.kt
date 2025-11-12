package com.fakhry.pomodojo.tools

import kotlinx.datetime.LocalDate
import kotlin.test.Test

/**
 * Helper test to generate SQL INSERT statements for 600 days of test data
 *
 * Run this test and copy the console output to generate_test_data.sql
 */
class GenerateSQLHelper {

    @Test
    fun `generate 600 days SQL statements`() {
        val data = TestDataGenerator.generateHistoryData(
            count = 600,
            seed = 42, // Use seed for reproducible data
        )

        val sql = TestDataGenerator.generateSQLInserts(data)

        println("\n=== COPY THE OUTPUT BELOW TO generate_test_data.sql ===\n")
        println(sql)
        println("\n=== END OF SQL ===\n")
        println("Total entries: ${data.size}")
        println(
            "Date range: ${data.first().dateStartedEpochMs} to ${data.last().dateStartedEpochMs}",
        )
    }

    @Test
    fun `generate SQL for specific year 2024 only`() {
        val data = TestDataGenerator.generateHistoryDataForDateRange(
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            seed = 2024,
        )

        val sql = TestDataGenerator.generateSQLInserts(data)

        println("\n=== 2024 FULL YEAR SQL (366 days - leap year) ===\n")
        println(sql)
        println("\n=== END OF SQL ===\n")
        println("Total entries: ${data.size}")
    }

    @Test
    fun `generate SQL for 2025 only`() {
        val data = TestDataGenerator.generateHistoryDataForDateRange(
            startDate = LocalDate(2025, 1, 1),
            endDate = LocalDate(2025, 12, 31),
            seed = 2025,
        )

        val sql = TestDataGenerator.generateSQLInserts(data)

        println("\n=== 2025 FULL YEAR SQL (365 days) ===\n")
        println(sql)
        println("\n=== END OF SQL ===\n")
        println("Total entries: ${data.size}")
    }
}
