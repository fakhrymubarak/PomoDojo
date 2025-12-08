package com.fakhry.pomodojo.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.designsystem.theme.PomoDojoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BgHeaderCanvas(
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable BoxScope.() -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Box(modifier = modifier.background(color = colorScheme.secondary)) {
            content()
        }

        Canvas(modifier = modifier.height(12.dp)) {
            val vw = 390f
            val vh = 128f

            // Scale factors so it adapts to whatever size this Canvas is
            val scaleFactorX = size.width / vw
            val scaleFactorY = size.height / vh

            // Oval size
            val cx = 195.5f * scaleFactorX
            val rx = 253.5f * scaleFactorX
            val cy = -50f * scaleFactorY
            val ry = 150f * scaleFactorY

            // Clip to the SVG viewport (like <clipPath> over the <rect width=390 height=128>)
            clipRect {
                drawOval(
                    color = colorScheme.secondary,
                    topLeft = Offset(cx - rx, cy - ry),
                    size = Size(rx * 2, ry * 2),
                )
            }
        }
    }
}

@Preview
@Composable
fun BgHeaderCanvasPreview() {
    PomoDojoTheme {
        BgHeaderCanvas {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Dashboard",
                style =
                MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                ),
            )
        }
    }
}
