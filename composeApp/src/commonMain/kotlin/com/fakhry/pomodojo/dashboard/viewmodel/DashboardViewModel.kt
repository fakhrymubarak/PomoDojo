package com.fakhry.pomodojo.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakhry.pomodojo.dashboard.model.HistoryCell
import com.fakhry.pomodojo.dashboard.model.HistorySectionUi
import com.fakhry.pomodojo.dashboard.model.intensityLevelForMinutes
import com.fakhry.pomodojo.focus.domain.repository.PomodoroSessionRepository
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val message: String, val code: Int) : DomainResult<Nothing>()
}

data class HistoryDomain(
    val date: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
)

data class PomodoroHistoryDomain(
    val focusMinutesThisYear: Int,
    val histories: List<HistoryDomain>,

    )

interface PomodoroHistoryRepository {
    fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain>
}

class PomodoroHistoryRepositoryImpl : PomodoroHistoryRepository {

    override fun getHistory(year: Int): DomainResult<PomodoroHistoryDomain> {
        // 144 dummy entries: years 2023..2026, months 1..12, days 1/10/20 (clamped to month length)
        val histories: List<HistoryDomain> = buildList {
            for (y in 2023..2025) {
                for (m in 1..12) {
                    val dim = daysInMonth(y, m)
                    val days = (1..28).toList()
                    for (d in days) {
                        val day = if (d <= dim) d else dim
                        val dateStr = formatDate(day, m, y)

                        // Deterministic pseudo-random-ish values (multiples of 25)
                        val focus = (((y * 37 + m * 13 + day) % 9) + 2) * 25   // 50..275
                        val breaks = focus / 5

                        add(HistoryDomain(dateStr, focus, breaks))
                    }
                }
            }
        }

        // Filter by requested year (no date libs needed)
        val filtered = histories.filter { extractYear(it.date) == year }

        // Aggregate total focus minutes for the year
        val focusMinutesThisYear = filtered.sumOf { it.focusMinutes }

        return DomainResult.Success(
            PomodoroHistoryDomain(
                focusMinutesThisYear = focusMinutesThisYear,
                histories = filtered
            )
        )
    }


    /** Helpers (KMP-safe, no java.time or String.format) */

    private fun formatDate(day: Int, month: Int, year: Int): String {
        val d = day.toString().padStart(2, '0')
        val m = month.toString().padStart(2, '0')
        val y = year.toString().padStart(4, '0')
        return "$d-$m-$y"
    }

    /** Helpers (no java.time) */
    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> error("Invalid month: $month")
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    private fun extractYear(date: String): Int =
        date.substring(date.lastIndexOf('-') + 1).toIntOrNull()
            ?: error("Invalid date format (expected dd-MM-yyyy): $date")
}

class DashboardViewModel(
    private val historyRepo: PomodoroHistoryRepository,
    private val repository: PreferencesRepository,
    private val focusRepository: PomodoroSessionRepository,
    private val dispatcher: DispatcherProvider,
    private val currentTimeProvider: CurrentTimeProvider,
) : ViewModel() {
    private val _hasActiveSession = MutableStateFlow(false)
    val hasActiveSession: StateFlow<Boolean> = _hasActiveSession.asStateFlow()

    private val _prefState = MutableStateFlow(PreferencesDomain())
    val prefState: StateFlow<PreferencesDomain> = _prefState.asStateFlow()

    private val _historyState = MutableStateFlow(HistorySectionUi())
    val historyState: StateFlow<HistorySectionUi> = _historyState.asStateFlow()

    init {
        viewModelScope.launch {
            async { checkHasActiveSession() }
            async { fetchPreferences() }
            async { fetchHistory() }
        }
    }

    suspend fun checkHasActiveSession() {
        val hasActiveSession = focusRepository.hasActiveSession()
        _hasActiveSession.value = hasActiveSession
    }

    suspend fun fetchPreferences() {
        repository.preferences.collect { preferences ->
            _prefState.value = preferences
        }
    }

    fun fetchHistory(selectedYear: Int = -1) = viewModelScope.launch(dispatcher.io) {
        @OptIn(ExperimentalTime::class)
        val currentYear = if (selectedYear < 0) {
            currentTimeProvider.nowInstant()
                .toLocalDateTime(TimeZone.UTC)
                .year
        } else {
            selectedYear
        }
        when (val result = historyRepo.getHistory(currentYear)) {
            is DomainResult.Success -> {
                val historySectionUi: HistorySectionUi =
                    result.data.mapToHistorySectionUi(selectedYear = currentYear)
                _historyState.update { historySectionUi }
            }

            is DomainResult.Error -> {}
        }
    }

    fun selectYear(year: Int) {
        _historyState.update { current ->
            if (year == current.selectedYear || year !in current.availableYears) {
                current
            } else {
                fetchHistory(year)
                current.copy(selectedYear = year)
            }
        }
    }
}

fun PomodoroHistoryDomain.mapToHistorySectionUi(
    selectedYear: Int,
): HistorySectionUi {
    val parsedEntries =
        histories.mapNotNull { it.toHistoryEntryOrNull() }.sortedBy { it.date }

    val yearEntries = parsedEntries.filter { it.date.year == selectedYear }
    val historyCells = buildHistoryCells(yearEntries, selectedYear)

    return HistorySectionUi(
        focusMinutesThisYear = focusMinutesThisYear,
        selectedYear = selectedYear,
        availableYears = persistentListOf(2023, 2024, 2025),
        cells = historyCells,
    )
}

private fun buildHistoryCells(
    entries: List<HistoryEntry>,
    year: Int,
): ImmutableList<ImmutableList<HistoryCell>> {
    val rows = mutableListOf<ImmutableList<HistoryCell>>()
    rows += dayLabelRow()

    val entriesByDate = entries.associateBy { it.date }
    Month.entries.toTypedArray()
        .sortedByDescending { it.ordinal }
        .forEach { month ->
            val dailyCells = generateDatesForMonth(year, month).map { date ->
                entriesByDate[date]?.toGraphCell() ?: EmptyGraphCell
            }
            dailyCells.chunked(7).forEachIndexed { index, chunk ->
                val paddedChunk =
                    if (chunk.size < 7) {
                        chunk + List(7 - chunk.size) { EmptyGraphCell }
                    } else {
                        chunk
                    }
                val row =
                    buildList {
                        add(
                            if (index == 0) {
                                HistoryCell.Text(month.toLabel())
                            } else {
                                HistoryCell.Empty
                            },
                        )
                        addAll(paddedChunk)
                    }.toPersistentList()
                rows += row
            }
        }

    return rows.toPersistentList()
}

private fun dayLabelRow(): ImmutableList<HistoryCell> =
    persistentListOf(
        HistoryCell.Empty,
        HistoryCell.Text("Mon"),
        HistoryCell.Empty,
        HistoryCell.Text("Wed"),
        HistoryCell.Empty,
        HistoryCell.Text("Fri"),
        HistoryCell.Empty,
        HistoryCell.Text("Sun"),
    )

private data class HistoryEntry(
    val date: LocalDate,
    val focusMinutes: Int,
    val breakMinutes: Int,
)

private fun HistoryDomain.toHistoryEntryOrNull(): HistoryEntry? {
    val parsedDate = date.toLocalDateOrNull() ?: return null
    return HistoryEntry(
        date = parsedDate,
        focusMinutes = focusMinutes,
        breakMinutes = breakMinutes,
    )
}

private fun HistoryEntry.toGraphCell(): HistoryCell.GraphLevel =
    HistoryCell.GraphLevel(
        intensityLevel = intensityLevelForMinutes(focusMinutes),
        focusMinutes = focusMinutes,
        breakMinutes = breakMinutes,
    )

private fun generateDatesForMonth(
    year: Int,
    month: Month,
): List<LocalDate> {
    val daysInMonth =
        when (month) {
            Month.JANUARY,
            Month.MARCH,
            Month.MAY,
            Month.JULY,
            Month.AUGUST,
            Month.OCTOBER,
            Month.DECEMBER,
                -> 31

            Month.APRIL,
            Month.JUNE,
            Month.SEPTEMBER,
            Month.NOVEMBER,
                -> 30

            Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
        }
    return (1..daysInMonth).map { day ->
        LocalDate(year, monthNumber = month.ordinal + 1, dayOfMonth = day)
    }
}

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

private fun String.toLocalDateOrNull(): LocalDate? {
    val normalized = trim()
    if (normalized.isEmpty()) return null

    runCatching { LocalDate.parse(normalized) }.getOrNull()?.let { return it }

    val separators = listOf("-", "/")
    separators.forEach { separator ->
        val parts = normalized.split(separator)
        if (parts.size != 3) return@forEach
        val isoFirst = parts[0].length == 4
        val isoLast = parts[2].length == 4
        when {
            isoFirst -> runCatching {
                LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            }.getOrNull()?.let { return it }

            isoLast -> runCatching {
                LocalDate(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
            }.getOrNull()?.let { return it }
        }
    }

    return null
}

private fun Month.toLabel(): String =
    name.take(3).lowercase().replaceFirstChar { it.uppercase() }

private val EmptyGraphCell = HistoryCell.GraphLevel(0, 0, 0)
