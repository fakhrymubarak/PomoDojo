package com.fakhry.pomodojo.focus.ui

import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import com.fakhry.pomodojo.focus.domain.usecase.CreatePomodoroSessionUseCase
import com.fakhry.pomodojo.focus.domain.usecase.CurrentTimeProvider
import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.usecase.SegmentCompletionSoundPlayer
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerType
import com.fakhry.pomodojo.preferences.domain.usecase.BuildHourSplitTimelineUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.BuildTimerSegmentsUseCase
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.utils.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        assertEquals(TimerStatusDomain.Running, state.activeSegment.timerStatus, "state=$state")
        assertEquals("01:00", state.activeSegment.timer.formattedTime, "state=$state")
        assertEquals(0f, state.activeSegment.timer.progress, "state=$state")
    }

    @Test
    fun `running timer updates remaining time and progress`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.awaitSessionStarted()

        advanceTimeBy(45_000)
        runCurrent()

        val state = viewModel.currentSnapshot()
        assertEquals(0.7f, state.activeSegment.timer.progress, 0.2f, "state=$state")
    }

    @Test
    fun `confirm finish emits dialog and completion side effects`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.awaitSessionStarted()

        val showDialog = async {
            viewModel.container.sideEffectFlow.first {
                it is PomodoroSessionSideEffect.ShowEndSessionDialog && it.isShown
            } as PomodoroSessionSideEffect.ShowEndSessionDialog
        }
        viewModel.onEndClicked()
        assertTrue(showDialog.await().isShown)

        val hideDialog = async {
            viewModel.container.sideEffectFlow.first {
                it is PomodoroSessionSideEffect.ShowEndSessionDialog && !it.isShown
            } as PomodoroSessionSideEffect.ShowEndSessionDialog
        }
        viewModel.onDismissConfirmEnd()
        assertTrue(!hideDialog.await().isShown)

        val completeEffect = async {
            viewModel.container.sideEffectFlow.first {
                it is PomodoroSessionSideEffect.OnSessionComplete
            }
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
                    status = TimerStatusDomain.Completed,
                    finishedAt = 1 * minuteMillis,
                ),
                timerSegment(
                    type = TimerType.SHORT_BREAK,
                    cycle = 1,
                    durationMs = minuteMillis,
                    status = TimerStatusDomain.Running,
                    finishedAt = 2 * minuteMillis,
                ),
                timerSegment(
                    type = TimerType.FOCUS,
                    cycle = 2,
                    durationMs = minuteMillis,
                    status = TimerStatusDomain.Initial,
                ),
                timerSegment(
                    type = TimerType.SHORT_BREAK,
                    cycle = 2,
                    durationMs = minuteMillis,
                    status = TimerStatusDomain.Initial,
                ),
            ),
        )
        val sessionRepository = FakeActiveSessionRepository(initialSession = storedSession)

        val viewModel = createViewModel(sessionRepository = sessionRepository)
        runCurrent()

        val state = viewModel.awaitSessionStarted()

        assertEquals(
            TimerStatusDomain.Running,
            state.activeSegment.timerStatus,
        )
        assertEquals(
            TimerType.SHORT_BREAK,
            state.activeSegment.type,
        )
        assertEquals(
            "00:30",
            state.activeSegment.timer.formattedTime,
        )
        assertEquals(
            listOf(
                TimerStatusDomain.Completed,
                TimerStatusDomain.Completed,
                TimerStatusDomain.Completed,
            ),
            state.timeline.segments.take(3).map { it.timerStatus },
        )
        assertEquals(
            TimerStatusDomain.Running,
            sessionRepository.storedSession!!.timeline.segments[3].timerStatus,
        )
    }

    @Test
    fun `segment completion triggers timer notification sound`() = runTest(dispatcher) {
        val soundPlayer = FakeSegmentCompletionSoundPlayer()
        val viewModel = createViewModel(soundPlayer = soundPlayer)
        runCurrent()
        viewModel.awaitSessionStarted()

        advanceTimeBy(minuteMillis)
        runCurrent()
        advanceUntilIdle()

        assertEquals(1, soundPlayer.playCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createViewModel(
        preferences: PreferencesDomain = PreferencesDomain(
            repeatCount = 1,
            focusMinutes = 1,
            breakMinutes = 1,
            longBreakEnabled = false,
            longBreakAfter = PreferencesDomain.DEFAULT_LONG_BREAK_AFTER,
            longBreakMinutes = 1,
        ),
        sessionRepository: FakeActiveSessionRepository = FakeActiveSessionRepository(),
        soundPlayer: SegmentCompletionSoundPlayer = FakeSegmentCompletionSoundPlayer(),
    ): PomodoroSessionViewModel {
        val currentTimeProvider = TestCurrentTimeProvider(testScheduler)
        val quoteRepository = FakeQuoteRepository()
        val preferencesRepository = FakePreferencesRepository(preferences)
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
            sessionRepository = sessionRepository,
            focusSessionNotifier = focusNotifier,
            segmentCompletionSoundPlayer = soundPlayer,
            dispatcher = dispatcherProvider,
        )
    }
}

private suspend fun PomodoroSessionViewModel.awaitSessionStarted(): PomodoroSessionUiState =
    container.stateFlow.first { it.totalCycle > 0 }

private fun PomodoroSessionViewModel.currentSnapshot(): PomodoroSessionUiState =
    container.stateFlow.value

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

private class FakePreferencesRepository(initial: PreferencesDomain) : PreferencesRepository {
    private val state = MutableStateFlow(initial)

    override val preferences: Flow<PreferencesDomain> = state.asStateFlow()

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

    override suspend fun updateAppTheme(theme: AppTheme) {
        state.update { it.copy(appTheme = theme) }
    }

    override suspend fun updateAlwaysOnDisplayEnabled(enabled: Boolean) {
        state.update { it.copy(alwaysOnDisplayEnabled = enabled) }
    }
}

private class FakeActiveSessionRepository(
    initialSession: PomodoroSessionDomain? = null,
) : com.fakhry.pomodojo.focus.domain.repository.ActiveSessionRepository {
    var storedSession: PomodoroSessionDomain? = initialSession

    override suspend fun getActiveSession(): PomodoroSessionDomain =
        storedSession ?: error("No active session stored for test")

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        storedSession = snapshot
    }

    override suspend fun updateActiveSession(snapshot: PomodoroSessionDomain) {
        storedSession = snapshot
    }

    override suspend fun completeSession(snapshot: PomodoroSessionDomain) {
        storedSession = null
    }

    override suspend fun clearActiveSession() {
        storedSession = null
    }

    override suspend fun hasActiveSession(): Boolean = storedSession != null
}

private class FakeFocusSessionNotifier : FocusSessionNotifier {
    override suspend fun schedule(snapshot: PomodoroSessionDomain) = Unit
    override suspend fun cancel(sessionId: String) = Unit
}

private class FakeSegmentCompletionSoundPlayer : SegmentCompletionSoundPlayer {
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
