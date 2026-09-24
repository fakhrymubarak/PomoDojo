package com.fakhry.pomodojo.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.fakhry.pomodojo.core.designsystem.theme.PomoDojoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Renders a "MM:SS"-style timer string so it never re-lays-out as digits change and the colon
 * reads as vertically centered against the digits — while keeping the app's proportional font.
 *
 * Each digit is centered in a fixed-width slot equal to the widest of `0-9` (measured at runtime
 * for the given [style]), so every column has a stable width. The colon is drawn as two dots
 * centered on the digits' optical midpoint rather than rendered from the font glyph (whose dots
 * sit low against tall bold digits). Any other character falls back to natural-width text.
 */
@Composable
fun MonospacedTimerText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val dotColor = style.color.takeOrElse { LocalContentColor.current }

    val metrics = remember(style, density) {
        val slotWidthPx = ('0'..'9').maxOf { measurer.measure(it.toString(), style).size.width }
        val real = measurer.measure("0", style)
        val fontSizePx = with(density) { style.fontSize.toPx() }
        // Lining figures are ~0.7em tall and sit on the baseline, so their optical center is ~0.35em
        // above the baseline — where the colon should sit, not the font's lower glyph position.
        TimerMetrics(
            slotWidthPx = slotWidthPx,
            heightPx = real.size.height,
            dotCenterYPx = real.firstBaseline - fontSizePx * FIGURE_HALF_HEIGHT_EM,
            dotRadiusPx = fontSizePx * 0.08f,
            dotGapPx = fontSizePx * 0.30f,
            colonWidthPx = fontSizePx * 0.34f,
        )
    }

    val slotWidth = with(density) { metrics.slotWidthPx.toDp() }
    val height = with(density) { metrics.heightPx.toDp() }
    val colonWidth = with(density) { metrics.colonWidthPx.toDp() }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        text.forEach { char ->
            when {
                char == ':' -> Box(Modifier.width(colonWidth).height(height)) {
                    Canvas(Modifier.matchParentSize()) {
                        val cx = size.width / 2f
                        drawCircle(
                            color = dotColor,
                            radius = metrics.dotRadiusPx,
                            center = Offset(cx, metrics.dotCenterYPx - metrics.dotGapPx / 2f),
                        )
                        drawCircle(
                            color = dotColor,
                            radius = metrics.dotRadiusPx,
                            center = Offset(cx, metrics.dotCenterYPx + metrics.dotGapPx / 2f),
                        )
                    }
                }

                char.isDigit() -> Box(
                    Modifier.width(slotWidth),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = char.toString(), style = style, maxLines = 1)
                }

                else -> Text(text = char.toString(), style = style, maxLines = 1)
            }
        }
    }
}

/** Lining figures are ~0.7em tall; half that is the offset of their optical center above baseline. */
private const val FIGURE_HALF_HEIGHT_EM = 0.35f

private data class TimerMetrics(
    val slotWidthPx: Int,
    val heightPx: Int,
    val dotCenterYPx: Float,
    val dotRadiusPx: Float,
    val dotGapPx: Float,
    val colonWidthPx: Float,
)

@Preview
@Composable
fun MonospacedTimerTextPreview() {
    PomoDojoTheme {
        val style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Column {
            MonospacedTimerText(text = "11:11", style = style)
            MonospacedTimerText(text = "48:00", style = style)
        }
    }
}
