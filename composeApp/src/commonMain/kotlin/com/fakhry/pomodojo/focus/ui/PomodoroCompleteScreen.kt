package com.fakhry.pomodojo.focus.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.Immutable
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

@Immutable
data class PomodoroCompletionUiState(
    val totalCyclesFinished: Int = 0,
    val totalFocusMinutes: Int = 0,
    val totalBreakMinutes: Int = 0,
)

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
        Column(modifier = Modifier.fillMaxSize()) {
            CompletionHeader(totalCycles = uiState.totalCyclesFinished)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CelebrationCard(message = celebration)
                    CompletionStatsCard(uiState = uiState)
                }

                Button(
                    onClick = onStartAnotherSession,
                    modifier = Modifier.fillMaxWidth(),
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
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.pomodoro_complete_header_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                ),
            )
            val safeTotalCycles = totalCycles.coerceAtLeast(0)
            val subtitle = pluralStringResource(
                Res.plurals.pomodoro_complete_header_cycles,
                safeTotalCycles,
                safeTotalCycles,
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
private fun CelebrationCard(message: CelebrationMessage, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center,
        ) {
            ConfettiBurst(modifier = Modifier.matchParentSize())
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = message.headline,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = message.subtitle,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
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
        headline = "Congratulations for finishing this cycle!",
        subtitle = "You showed up and focused. That consistency is building momentum.",
    ),
    CelebrationMessage(
        headline = "Focus streak complete!",
        subtitle = "Enjoy that break—you've earned it.",
    ),
    CelebrationMessage(
        headline = "Pomodoro mastered.",
        subtitle = "Those minutes are stacking up to serious progress.",
    ),
    CelebrationMessage(
        headline = "Nice work staying on task!",
        subtitle = "Keep this energy going for your next deep-work sprint.",
    ),
    CelebrationMessage(
        headline = "That was a strong focus block.",
        subtitle = "Take a breather, then crush the next cycle.",
    ),
)

private const val CONFETTI_COUNT = 28

private data class ConfettiPiece(
    val startXFraction: Float,
    val widthFraction: Float,
    val heightFraction: Float,
    val baseRotation: Float,
    val rotationSpeed: Float,
    val fallSpeedMultiplier: Float,
    val horizontalDrift: Float,
    val horizontalFrequency: Float,
    val timeOffset: Float,
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
            val fallProgress =
                ((animationTimeSeconds * piece.fallSpeedMultiplier) + piece.timeOffset)
                    .wrapToUnitInterval()
            val verticalFraction = (fallProgress * 1.2f) - 0.1f
            val swayOffset = (
                sin(
                    (fallProgress + piece.timeOffset) *
                        piece.horizontalFrequency *
                        2f * PI,
                ).toFloat() * piece.horizontalDrift
                )
            val horizontalFraction = (piece.startXFraction + swayOffset).wrapToUnitInterval()
            val rotation = piece.baseRotation + (animationTimeSeconds * piece.rotationSpeed)

            val pivot = Offset(horizontalFraction * size.width, verticalFraction * size.height)
            val width = piece.widthFraction * size.width
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
        timeOffset = random.nextFloat(),
        color = colors[random.nextInt(colors.size)],
    )
}

@Composable
private fun rememberConfettiAnimationTime(): Float {
    val elapsedSeconds = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var previousFrameTime = withFrameNanos { it }
        while (true) {
            val frameTime = withFrameNanos { it }
            val deltaSeconds = ((frameTime - previousFrameTime).coerceAtLeast(0L)) / 1_000_000_000f
            previousFrameTime = frameTime
            val total = elapsedSeconds.floatValue + deltaSeconds
            elapsedSeconds.floatValue = if (total > 120f) total - 120f else total
        }
    }
    return elapsedSeconds.floatValue
}

private fun Float.wrapToUnitInterval(): Float {
    val wrapped = this % 1f
    return if (wrapped < 0f) wrapped + 1f else wrapped
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
