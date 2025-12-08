package com.fakhry.pomodojo.features.focus.domain.usecase

import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionRepository
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences
import com.fakhry.pomodojo.domain.preferences.repository.PreferencesRepository
import com.fakhry.pomodojo.features.focus.domain.repository.QuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CreatePomodoroSessionUseCaseTest {
    @Test
    fun `creates session with preferences and quote`() = runTest {
        val now = 1_000_000L
        val expectedQuote = QuoteContent(
            id = "test-quote",
            text = "Test quote text",
            character = "Test Character",
            sourceTitle = "Test Anime",
            metadata = null,
        )
        val preferences = PomodoroPreferences(
            focusMinutes = 25,
            breakMinutes = 5,
            repeatCount = 4,
            longBreakEnabled = true,
            longBreakAfter = 2,
            longBreakMinutes = 15,
        )

        val quoteRepo = FakeQuoteRepository(expectedQuote)
        val preferencesRepo = FakePreferencesRepository(preferences)
        val sessionRepo = FakeActiveSessionRepository()
        val useCase = CreatePomodoroSessionUseCase(
            sessionRepository = sessionRepo,
            quoteRepo = quoteRepo,
            preferencesRepo = preferencesRepo,
            timelineBuilder = BuildTimerSegmentsUseCase(),
            hourSplitter = BuildHourSplitTimelineUseCase(),
            dispatcher = DispatcherProvider(Dispatchers.Unconfined),
        )

        val session = useCase(now)

        assertNotNull(session)
        assertEquals(preferences.repeatCount, session.totalCycle)
        assertEquals(now, session.startedAtEpochMs)
        assertEquals(0L, session.elapsedPauseEpochMs)
        assertEquals(expectedQuote, session.quote)
        assertTrue(session.timeline.segments.isNotEmpty())
        assertTrue(session.timeline.hourSplits.isNotEmpty())
    }

    @Test
    fun `saves session to repository`() = runTest {
        val now = 2_000_000L
        val quoteRepo = FakeQuoteRepository(QuoteContent.DEFAULT_QUOTE)
        val preferencesRepo = FakePreferencesRepository(PomodoroPreferences())
        val sessionRepo = FakeActiveSessionRepository()
        val useCase = CreatePomodoroSessionUseCase(
            sessionRepository = sessionRepo,
            quoteRepo = quoteRepo,
            preferencesRepo = preferencesRepo,
            timelineBuilder = BuildTimerSegmentsUseCase(),
            hourSplitter = BuildHourSplitTimelineUseCase(),
            dispatcher = DispatcherProvider(Dispatchers.Unconfined),
        )

        val session = useCase(now)

        assertTrue(sessionRepo.saveSessionCalled)
        assertEquals(session, sessionRepo.lastSavedSession)
    }

    @Test
    fun `creates session with custom preferences`() = runTest {
        val now = 3_000_000L
        val customPreferences = PomodoroPreferences(
            focusMinutes = 50,
            breakMinutes = 10,
            repeatCount = 2,
            longBreakEnabled = false,
        )

        val quoteRepo = FakeQuoteRepository(QuoteContent.DEFAULT_QUOTE)
        val preferencesRepo = FakePreferencesRepository(customPreferences)
        val sessionRepo = FakeActiveSessionRepository()
        val useCase = CreatePomodoroSessionUseCase(
            sessionRepository = sessionRepo,
            quoteRepo = quoteRepo,
            preferencesRepo = preferencesRepo,
            timelineBuilder = BuildTimerSegmentsUseCase(),
            hourSplitter = BuildHourSplitTimelineUseCase(),
            dispatcher = DispatcherProvider(Dispatchers.Unconfined),
        )

        val session = useCase(now)

        assertEquals(2, session.totalCycle)
        assertEquals(now, session.startedAtEpochMs)
        // With 2 cycles and no long break: 2 focus + 2 short breaks = 4 segments
        assertEquals(4, session.timeline.segments.size)
    }

    @Test
    fun `timeline builder receives correct parameters`() = runTest {
        val now = 4_000_000L
        val preferences = PomodoroPreferences(
            focusMinutes = 30,
            breakMinutes = 10,
            repeatCount = 3,
        )

        val quoteRepo = FakeQuoteRepository(QuoteContent.DEFAULT_QUOTE)
        val preferencesRepo = FakePreferencesRepository(preferences)
        val sessionRepo = FakeActiveSessionRepository()
        val useCase = CreatePomodoroSessionUseCase(
            sessionRepository = sessionRepo,
            quoteRepo = quoteRepo,
            preferencesRepo = preferencesRepo,
            timelineBuilder = BuildTimerSegmentsUseCase(),
            hourSplitter = BuildHourSplitTimelineUseCase(),
            dispatcher = DispatcherProvider(Dispatchers.Unconfined),
        )

        val session = useCase(now)

        // Verify first segment has correct timing
        val firstSegment = session.timeline.segments.first()
        val expectedFinishTime = now + (30 * 60_000L)
        assertEquals(expectedFinishTime, firstSegment.timer.finishedInMillis)
    }

    @Test
    fun `handles long break configuration`() = runTest {
        val now = 5_000_000L
        val preferences = PomodoroPreferences(
            focusMinutes = 25,
            breakMinutes = 5,
            repeatCount = 4,
            longBreakEnabled = true,
            longBreakAfter = 2,
            longBreakMinutes = 20,
        )

        val quoteRepo = FakeQuoteRepository(QuoteContent.DEFAULT_QUOTE)
        val preferencesRepo = FakePreferencesRepository(preferences)
        val sessionRepo = FakeActiveSessionRepository()
        val useCase = CreatePomodoroSessionUseCase(
            sessionRepository = sessionRepo,
            quoteRepo = quoteRepo,
            preferencesRepo = preferencesRepo,
            timelineBuilder = BuildTimerSegmentsUseCase(),
            hourSplitter = BuildHourSplitTimelineUseCase(),
            dispatcher = DispatcherProvider(Dispatchers.Unconfined),
        )

        val session = useCase(now)

        // 4 focus + 2 short breaks + 2 long breaks = 8 segments
        assertEquals(8, session.timeline.segments.size)
        assertTrue(session.timeline.hourSplits.isNotEmpty())
    }
}

private class FakeQuoteRepository(private val quote: QuoteContent) : QuoteRepository {
    override suspend fun randomQuote(): QuoteContent = quote

    override suspend fun getById(id: String): QuoteContent = quote
}

private class FakePreferencesRepository(initial: PomodoroPreferences) : PreferencesRepository {
    private val state = MutableStateFlow(initial)

    override val preferences: Flow<PomodoroPreferences> = state.asStateFlow()

    override suspend fun updateRepeatCount(value: Int) {
        state.value = state.value.copy(repeatCount = value)
    }

    override suspend fun updateFocusMinutes(value: Int) {
        state.value = state.value.copy(focusMinutes = value)
    }

    override suspend fun updateBreakMinutes(value: Int) {
        state.value = state.value.copy(breakMinutes = value)
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        state.value = state.value.copy(longBreakEnabled = enabled)
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        state.value = state.value.copy(longBreakAfter = value)
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        state.value = state.value.copy(longBreakMinutes = value)
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        state.value = state.value.copy(alwaysOnDisplayEnabled = enabled)
    }
}

private class FakeActiveSessionRepository : ActiveSessionRepository {
    var saveSessionCalled = false
    var lastSavedSession: PomodoroSessionDomain? = null

    override suspend fun hasActiveSession(): Boolean = lastSavedSession != null

    override suspend fun getActiveSession(): PomodoroSessionDomain =
        lastSavedSession ?: PomodoroSessionDomain()

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        saveSessionCalled = true
        lastSavedSession = snapshot
    }

    override suspend fun clearActiveSession() {
        lastSavedSession = null
    }
}
