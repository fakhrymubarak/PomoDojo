package com.fakhry.pomodojo.focus.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.utils.compose.Expanded
import com.fakhry.pomodojo.focus.ui.model.PomodoroCompletionUiState
import com.fakhry.pomodojo.generated.resources.Res
import com.fakhry.pomodojo.generated.resources.minutes
import com.fakhry.pomodojo.generated.resources.pomodoro_complete_break_label
import com.fakhry.pomodojo.generated.resources.pomodoro_complete_cycles_label
import com.fakhry.pomodojo.generated.resources.pomodoro_complete_focus_label
import com.fakhry.pomodojo.generated.resources.pomodoro_complete_header_cycles
import com.fakhry.pomodojo.generated.resources.pomodoro_complete_header_title
import com.fakhry.pomodojo.generated.resources.pomodoro_complete_start_another
import com.fakhry.pomodojo.generated.resources.pomodoro_complete_summary_title
import com.fakhry.pomodojo.ui.components.BgHeaderCanvas
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PomodoroCompleteScreen(
    uiState: PomodoroCompletionUiState,
    onStartAnotherSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val celebration = rememberCelebrationMessage()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(Modifier.fillMaxSize()) {
            CompletionHeader(totalCycles = uiState.totalCyclesFinished)
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Expanded()
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = celebration.headline,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Text(
                            text = celebration.subtitle,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )

                        CompletionStatsCard(uiState = uiState)

                        Button(
                            onClick = onStartAnotherSession,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 24.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(
                                text = stringResource(Res.string.pomodoro_complete_start_another),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }
                    Expanded()
                }
                ConfettiBurst(modifier = Modifier.matchParentSize())
            }
        }
    }
}

@Composable
private fun CompletionHeader(totalCycles: Int, modifier: Modifier = Modifier) {
    BgHeaderCanvas {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.pomodoro_complete_header_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                ),
            )
            val subtitle = pluralStringResource(
                Res.plurals.pomodoro_complete_header_cycles,
                totalCycles,
                totalCycles,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                ),
            )
        }
    }
}

@Composable
private fun CompletionStatsCard(uiState: PomodoroCompletionUiState, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.pomodoro_complete_summary_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            CompletionStatRow(
                label = stringResource(Res.string.pomodoro_complete_cycles_label),
                value = uiState.totalCyclesFinished.toString(),
            )
            CompletionStatRow(
                label = stringResource(Res.string.pomodoro_complete_focus_label),
                value = pluralStringResource(
                    Res.plurals.minutes,
                    uiState.totalFocusMinutes,
                    uiState.totalFocusMinutes,
                ),
            )
            CompletionStatRow(
                label = stringResource(Res.string.pomodoro_complete_break_label),
                value = pluralStringResource(
                    Res.plurals.minutes,
                    uiState.totalBreakMinutes,
                    uiState.totalBreakMinutes,
                ),
            )
        }
    }
}

@Composable
private fun CompletionStatRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun rememberCelebrationMessage(): CelebrationMessage {
    val index = remember { Random.nextInt(celebrationMessages.size) }
    return celebrationMessages[index]
}

private data class CelebrationMessage(val headline: String, val subtitle: String)
private val celebrationMessages = listOf(
    CelebrationMessage(
        headline = "🎉 Congratulations for finishing this cycle!",
        subtitle = "You showed up and focused. That consistency is building momentum.",
    ),
    CelebrationMessage(
        headline = "🔥 Focus streak complete!",
        subtitle = "Enjoy that break—you've earned it.",
    ),
    CelebrationMessage(
        headline = "🧠 Pomodoro mastered.",
        subtitle = "Those minutes are stacking up to serious progress.",
    ),
    CelebrationMessage(
        headline = "✅ Nice work staying on task!",
        subtitle = "Keep this energy going for your next deep-work sprint.",
    ),
    CelebrationMessage(
        headline = "💪 That was a strong focus block.",
        subtitle = "Take a breather, then crush the next cycle.",
    ),
    CelebrationMessage(
        headline = "🚀 Another deep-work win!",
        subtitle = "This block nudged you closer to the work that really matters.",
    ),
    CelebrationMessage(
        headline = "🌱 Tiny habits, big results.",
        subtitle = "Each finished cycle is planting seeds for tomorrow's wins.",
    ),
    CelebrationMessage(
        headline = "⏱️ Timer down, progress up.",
        subtitle = "Every focused minute is compounding behind the scenes.",
    ),
    CelebrationMessage(
        headline = "🧩 Another piece in the puzzle.",
        subtitle = "Keep placing pieces—your bigger picture is coming together.",
    ),
    CelebrationMessage(
        headline = "🌟 You showed up for yourself.",
        subtitle = "Protecting this time is how goals quietly become reality.",
    ),
    CelebrationMessage(
        headline = "📈 Focus level up!",
        subtitle = "Stay consistent and that line will only keep rising.",
    ),
    CelebrationMessage(
        headline = "🛡️ Distractions defeated.",
        subtitle = "You chose intention over impulse. That's rare and powerful.",
    ),
    CelebrationMessage(
        headline = "🎯 Bullseye focus achieved.",
        subtitle = "Aim, commit, execute—you're building that reflex.",
    ),
    CelebrationMessage(
        headline = "🧘 Break time unlocked.",
        subtitle = "Rest well now so your next focus round can hit even harder.",
    ),
    CelebrationMessage(
        headline = "🏗️ Brick by brick, you’re building.",
        subtitle = "Keep stacking deliberate effort; the structure is taking shape.",
    ),
    CelebrationMessage(
        headline = "🏁 You crossed another finish line.",
        subtitle = "Savor this win, then set up the next lap.",
    ),
    CelebrationMessage(
        headline = "🔄 Cycle complete, growth continues.",
        subtitle = "Every repetition tightens your focus muscle.",
    ),
    CelebrationMessage(
        headline = "🌞 You made this block count.",
        subtitle = "Even small sessions like this reshape your trajectory.",
    ),
    CelebrationMessage(
        headline = "🧱 Solid block of focus logged.",
        subtitle = "Moments like this are the foundation of long-term mastery.",
    ),
    CelebrationMessage(
        headline = "✨ Future-you is already grateful.",
        subtitle = "These sessions are the quiet investments that change everything.",
    ),
)

private const val CONFETTI_COUNT = 50
private const val CONFETTI_EMISSION_DURATION_SECONDS = 5f
private const val CONFETTI_ANIMATION_TOTAL_SECONDS =
    CONFETTI_EMISSION_DURATION_SECONDS + 3f

private data class ConfettiPiece(
    val startXFraction: Float,
    val widthFraction: Float,
    val heightFraction: Float,
    val baseRotation: Float,
    val rotationSpeed: Float,
    val fallSpeedMultiplier: Float,
    val horizontalDrift: Float,
    val horizontalFrequency: Float,
    val startDelaySeconds: Float,
    val wavePhase: Float,
    val color: Color,
)

@Composable
private fun ConfettiBurst(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val confettiColors = listOf(
        scheme.primary,
        scheme.secondary,
        Color(0xFFFFC857),
        Color(0xFF51BFE3),
        Color(0xFFFF6F91),
    )

    val pieces = remember(confettiColors) {
        generateConfettiPieces(colors = confettiColors)
    }
    val animationTimeSeconds = rememberConfettiAnimationTime()

    Canvas(modifier = modifier) {
        pieces.forEach { piece ->
            val timeSinceStart = animationTimeSeconds - piece.startDelaySeconds
            if (timeSinceStart < 0f) {
                return@forEach
            }
            val fallProgress = timeSinceStart * piece.fallSpeedMultiplier
            val verticalFraction = (fallProgress * 1.2f) - 0.1f
            if (verticalFraction > 1.1f) {
                return@forEach
            }

            val swayAngle =
                (fallProgress + piece.wavePhase) * piece.horizontalFrequency * 2f * PI
            val horizontalFraction = (
                piece.startXFraction + (sin(swayAngle).toFloat() * piece.horizontalDrift)
                ).coerceIn(0f, 1f)
            val rotation = piece.baseRotation + (timeSinceStart * piece.rotationSpeed)

            val pivot = Offset(horizontalFraction * size.width, verticalFraction * size.height)
            val width = piece.widthFraction * size.width * 0.5f
            val height = piece.heightFraction * size.height
            withTransform({
                rotate(degrees = rotation, pivot = pivot)
            }) {
                drawRoundRect(
                    color = piece.color,
                    topLeft = Offset(pivot.x - width / 2f, pivot.y - height / 2f),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(width * 0.3f, height * 0.3f),
                )
            }
        }
    }
}

private fun generateConfettiPieces(
    colors: List<Color>,
    count: Int = CONFETTI_COUNT,
    random: Random = Random,
): List<ConfettiPiece> = List(count) {
    val rotationDirection = if (random.nextBoolean()) 1f else -1f
    ConfettiPiece(
        startXFraction = random.nextFloat().coerceIn(0.05f, 0.95f),
        widthFraction = random.nextFloat().times(0.025f) + 0.015f,
        heightFraction = random.nextFloat().times(0.015f) + 0.008f,
        baseRotation = random.nextFloat() * 360f,
        rotationSpeed = (random.nextFloat().times(150f) + 60f) * rotationDirection,
        fallSpeedMultiplier = random.nextFloat().times(0.6f) + 0.7f,
        horizontalDrift = random.nextFloat().times(0.15f) + 0.02f,
        horizontalFrequency = random.nextFloat().times(1.2f) + 0.6f,
        startDelaySeconds = random.nextFloat() * CONFETTI_EMISSION_DURATION_SECONDS,
        wavePhase = random.nextFloat() * (2f * PI).toFloat(),
        color = colors[random.nextInt(colors.size)],
    )
}

@Composable
private fun rememberConfettiAnimationTime(): Float {
    val elapsedSeconds = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var previousFrameTime = withFrameNanos { it }
        while (elapsedSeconds.floatValue < CONFETTI_ANIMATION_TOTAL_SECONDS) {
            val frameTime = withFrameNanos { it }
            val deltaSeconds = ((frameTime - previousFrameTime).coerceAtLeast(0L)) / 1_000_000_000f
            previousFrameTime = frameTime
            val updated = (elapsedSeconds.floatValue + deltaSeconds)
                .coerceAtMost(CONFETTI_ANIMATION_TOTAL_SECONDS)
            elapsedSeconds.floatValue = updated
        }
    }
    return elapsedSeconds.floatValue
}

@Preview
@Composable
private fun PomodoroCompleteScreenPreview() {
    PomoDojoTheme {
        PomodoroCompleteScreen(
            uiState = PomodoroCompletionUiState(
                totalCyclesFinished = 4,
                totalFocusMinutes = 150,
                totalBreakMinutes = 45,
            ),
            onStartAnotherSession = {},
        )
    }
}
