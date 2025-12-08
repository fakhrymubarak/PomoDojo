package com.fakhry.pomodojo.features.focus.ui.viewmodel

import com.fakhry.pomodojo.core.designsystem.model.TimerStatusUi
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.notification.PomodoroSessionNotifier
import com.fakhry.pomodojo.core.notification.SoundPlayer
import com.fakhry.pomodojo.core.utils.date.CurrentTimeProvider
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.domain.history.model.PomodoroHistoryDomain
import com.fakhry.pomodojo.domain.history.repository.HistorySessionRepository
import com.fakhry.pomodojo.domain.pomodoro.model.PomodoroSessionDomain
import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimelineDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerSegmentsDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerStatusDomain
import com.fakhry.pomodojo.domain.pomodoro.model.timeline.TimerType
import com.fakhry.pomodojo.domain.pomodoro.repository.ActiveSessionRepository
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.domain.pomodoro.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.domain.preferences.model.PomodoroPreferences
import com.fakhry.pomodojo.domain.preferences.repository.PreferencesRepository
import com.fakhry.pomodojo.features.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.features.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionSideEffect
import com.fakhry.pomodojo.features.focus.ui.model.PomodoroSessionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.concurrent.Volatile
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroSessionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val minuteMillis = 60_000L

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `session starts immediately with active focus segment`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        val state = viewModel.awaitSessionStarted()

        assertEquals(1, state.totalCycle, "state=$state")
        assertEquals(TimerStatusUi.RUNNING, state.activeSegment.timerStatus, "state=$state")
        assertEquals("01:00", state.activeSegment.timer.formattedTime, "state=$state")
        assertEquals(0f, state.activeSegment.timer.progress, "state=$state")
    }

    @Test
    fun `confirm finish emits dialog and completion side effects`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.awaitSessionStarted()

        val showDialog = async {
            viewModel.container.sideEffectFlow
                .filterIsInstance<PomodoroSessionSideEffect.ShowEndSessionDialog>()
                .first { it.isShown }
        }
        viewModel.onEndClicked()
        assertTrue(showDialog.await().isShown)

        val hideDialog = async {
            viewModel.container.sideEffectFlow
                .filterIsInstance<PomodoroSessionSideEffect.ShowEndSessionDialog>()
                .first { !it.isShown }
        }
        viewModel.onDismissConfirmEnd()
        assertTrue(!hideDialog.await().isShown)

        val completeEffect = async {
            viewModel.container.sideEffectFlow
                .filterIsInstance<PomodoroSessionSideEffect.OnSessionComplete>()
                .first()
        }
        viewModel.onConfirmFinish()
        assertTrue(completeEffect.await() is PomodoroSessionSideEffect.OnSessionComplete)
    }

    @Test
    fun `restoring session fast forwards overdue segments`() = runTest(dispatcher) {
        val elapsedSinceStart = 3 * minuteMillis + 30 * 1_000L
        advanceTimeBy(elapsedSinceStart)

        val storedSession = activeSessionSnapshot(
            segments = listOf(
                timerSegment(
                    type = TimerType.FOCUS,
                    cycle = 1,
                    durationMs = minuteMillis,
                    status = TimerStatusDomain.COMPLETED,
                    finishedAt = 1 * minuteMillis,
                ),
                timerSegment(
                    type = TimerType.SHORT_BREAK,
                    cycle = 1,
                    durationMs = minuteMillis,
                    status = TimerStatusDomain.RUNNING,
                    finishedAt = 2 * minuteMillis,
                ),
                timerSegment(
                    type = TimerType.FOCUS,
                    cycle = 2,
                    durationMs = minuteMillis,
                    status = TimerStatusDomain.INITIAL,
                ),
                timerSegment(
                    type = TimerType.SHORT_BREAK,
                    cycle = 2,
                    durationMs = minuteMillis,
                    status = TimerStatusDomain.INITIAL,
                ),
            ),
        )
        val sessionRepository = FakeActiveSessionRepository(initialSession = storedSession)

        val viewModel = createViewModel(sessionRepository = sessionRepository)
        runCurrent()

        val state = viewModel.awaitSessionStarted()

        assertEquals(
            TimerStatusUi.RUNNING,
            state.activeSegment.timerStatus,
        )
        assertEquals(
            TimerTypeUi.SHORT_BREAK,
            state.activeSegment.type,
        )
        assertEquals(
            "00:30",
            state.activeSegment.timer.formattedTime,
        )
        assertEquals(
            listOf(
                TimerStatusUi.COMPLETED,
                TimerStatusUi.COMPLETED,
                TimerStatusUi.COMPLETED,
            ),
            state.timeline.segments.take(3).map { it.timerStatus },
        )
        assertEquals(
            TimerStatusDomain.RUNNING,
            sessionRepository.storedSession!!.timeline.segments[3].timerStatus,
        )
    }

    @Test
    fun `segment completion triggers timer notification sound`() = runTest(dispatcher) {
        val soundPlayer = FakeSoundPlayer()
        val viewModel = createViewModel(soundPlayer = soundPlayer)
        runCurrent()
        viewModel.awaitSessionStarted()

        advanceTimeBy(minuteMillis)
        runCurrent()
        advanceUntilIdle()

        // With breaks after last cycle, advanceUntilIdle() completes both segments
        // Focus completes (1 sound) + Break completes (1 sound) = 2 sounds total
        assertEquals(2, soundPlayer.playCount)
    }

    @Test
    fun `togglePauseResume pauses and resumes the active segment`() = runTest(dispatcher) {
        val preferencesRepository = FakePreferencesRepository(
            PomodoroPreferences(
                repeatCount = 1,
                focusMinutes = 1,
                breakMinutes = 1,
                longBreakEnabled = false,
            ),
        )
        val sessionRepository = FakeActiveSessionRepository()
        val viewModel =
            createViewModel(
                preferencesRepositoryOverride = preferencesRepository,
                sessionRepository = sessionRepository,
            )
        runCurrent()
        viewModel.awaitSessionStarted()

        viewModel.togglePauseResume()
        advanceUntilIdle()
        assertTrue(sessionRepository.storedSession != null)

        viewModel.togglePauseResume()
        runCurrent()
        assertTrue(sessionRepository.storedSession != null)
    }

    @Test
    fun `togglePauseResume completes running segment when timer elapsed`() = runTest(dispatcher) {
        val sessionRepository = FakeActiveSessionRepository()
        val viewModel = createViewModel(sessionRepository = sessionRepository)
        runCurrent()
        viewModel.awaitSessionStarted()

        advanceTimeBy(minuteMillis)
        runCurrent()

        viewModel.togglePauseResume()
        advanceUntilIdle()

        val updatedTimeline = viewModel.container.stateFlow.value.timeline.segments
        assertEquals(TimerStatusUi.COMPLETED, updatedTimeline.first().timerStatus)
        assertEquals(
            TimerTypeUi.SHORT_BREAK,
            viewModel.container.stateFlow.value.activeSegment.type,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createViewModel(
        preferences: PomodoroPreferences = PomodoroPreferences(
            repeatCount = 1,
            focusMinutes = 1,
            breakMinutes = 1,
            longBreakEnabled = false,
            longBreakAfter = PomodoroPreferences.DEFAULT_LONG_BREAK_AFTER,
            longBreakMinutes = 1,
        ),
        sessionRepository: FakeActiveSessionRepository = FakeActiveSessionRepository(),
        soundPlayer: SoundPlayer = FakeSoundPlayer(),
        preferencesRepositoryOverride: FakePreferencesRepository? = null,
    ): PomodoroSessionViewModel {
        val currentTimeProvider = TestCurrentTimeProvider(testScheduler)
        val quoteRepository = FakeQuoteRepository()
        val preferencesRepository =
            preferencesRepositoryOverride ?: FakePreferencesRepository(preferences)
        val dispatcherProvider = DispatcherProvider(dispatcher)

        val focusNotifier = FakeFocusSessionNotifier()
        val createSessionUseCase = CreatePomodoroSessionUseCase(
            sessionRepository = sessionRepository,
            quoteRepo = quoteRepository,
            preferencesRepo = preferencesRepository,
            timelineBuilder = BuildTimerSegmentsUseCase(),
            hourSplitter = BuildHourSplitTimelineUseCase(),
            dispatcher = dispatcherProvider,
        )

        return PomodoroSessionViewModel(
            currentTimeProvider = currentTimeProvider,
            createPomodoroSessionUseCase = createSessionUseCase,
            preferencesRepository = preferencesRepository,
            sessionRepository = sessionRepository,
            historyRepository = FakeHistorySessionRepository(),
            pomodoroSessionNotifier = focusNotifier,
            soundPlayer = soundPlayer,
            dispatcher = dispatcherProvider,
        )
    }
}

private suspend fun PomodoroSessionViewModel.awaitSessionStarted(): PomodoroSessionUiState =
    container.stateFlow.first { it.totalCycle > 0 }

private class FakeQuoteRepository(
    private val quote: QuoteContent = QuoteContent(
        id = "quote-id",
        text = "Stay focused",
        character = "Sensei",
    ),
) : QuoteRepository {
    override suspend fun randomQuote(): QuoteContent = quote

    override suspend fun getById(id: String): QuoteContent = quote
}

private class FakePreferencesRepository(initial: PomodoroPreferences) : PreferencesRepository {
    private val state = MutableStateFlow(initial)

    override val preferences: Flow<PomodoroPreferences> = state.asStateFlow()

    override suspend fun updateRepeatCount(value: Int) {
        state.update { it.copy(repeatCount = value) }
    }

    override suspend fun updateFocusMinutes(value: Int) {
        state.update { it.copy(focusMinutes = value) }
    }

    override suspend fun updateBreakMinutes(value: Int) {
        state.update { it.copy(breakMinutes = value) }
    }

    override suspend fun updateLongBreakEnabled(enabled: Boolean) {
        state.update { it.copy(longBreakEnabled = enabled) }
    }

    override suspend fun updateLongBreakAfter(value: Int) {
        state.update { it.copy(longBreakAfter = value) }
    }

    override suspend fun updateLongBreakMinutes(value: Int) {
        state.update { it.copy(longBreakMinutes = value) }
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        state.update { it.copy(alwaysOnDisplayEnabled = enabled) }
    }
}

private class FakeActiveSessionRepository(
    initialSession: PomodoroSessionDomain? = null,
) : ActiveSessionRepository {
    var storedSession: PomodoroSessionDomain? = initialSession

    override suspend fun getActiveSession(): PomodoroSessionDomain =
        storedSession ?: PomodoroSessionDomain()

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        storedSession = snapshot
    }

    override suspend fun clearActiveSession() {
        storedSession = null
    }

    override suspend fun hasActiveSession(): Boolean = storedSession != null
}

private class FakeHistorySessionRepository : HistorySessionRepository {
    val insertedSessions = mutableListOf<PomodoroSessionDomain>()

    override suspend fun insertHistory(session: PomodoroSessionDomain) {
        insertedSessions.add(session)
    }

    override suspend fun getHistory(
        year: Int,
    ): com.fakhry.pomodojo.domain.common.DomainResult<PomodoroHistoryDomain> =
        com.fakhry.pomodojo.domain.common.DomainResult.Success(
            PomodoroHistoryDomain(
                focusMinutesThisYear = 0,
                availableYears = emptyList(),
                histories = emptyList(),
            ),
        )
}

private class FakeFocusSessionNotifier : PomodoroSessionNotifier {
    override suspend fun schedule(snapshot: PomodoroSessionDomain) = Unit
    override suspend fun cancel(sessionId: String) = Unit
}

private class FakeSoundPlayer : SoundPlayer {
    @Volatile
    var playCount: Int = 0

    override fun playSegmentCompleted() {
        playCount += 1
    }
}

@OptIn(ExperimentalTime::class)
private class TestCurrentTimeProvider(private val scheduler: TestCoroutineScheduler) :
    CurrentTimeProvider {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun now(): Long = scheduler.currentTime

    override fun nowInstant(): Instant = Instant.fromEpochMilliseconds(now())
}

private fun activeSessionSnapshot(
    segments: List<TimerSegmentsDomain>,
    totalCycle: Int = 4,
): PomodoroSessionDomain = PomodoroSessionDomain(
    totalCycle = totalCycle,
    startedAtEpochMs = 0L,
    elapsedPauseEpochMs = 0L,
    timeline = TimelineDomain(
        segments = segments,
        hourSplits = emptyList(),
    ),
    quote = QuoteContent.DEFAULT_QUOTE,
)

private fun timerSegment(
    type: TimerType,
    cycle: Int,
    durationMs: Long,
    status: TimerStatusDomain,
    finishedAt: Long = 0L,
): TimerSegmentsDomain = TimerSegmentsDomain(
    type = type,
    cycleNumber = cycle,
    timer = TimerDomain(
        durationEpochMs = durationMs,
        finishedInMillis = finishedAt,
    ),
    timerStatus = status,
)
