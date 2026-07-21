package com.fakhry.pomodojo.core.designsystem.dialog

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * The shared visual card for both the window dialog ([PomoDialog]) and overlay modals
 * ([PomoModalOverlay]). No window and no scrim of its own — just the titled card with an
 * optional [message] and [confirm] / [dismiss] actions.
 *
 * The card consumes tap gestures so a dismiss-on-scrim overlay ignores taps that land on it.
 */
@Composable
fun DialogCard(
    title: String,
    confirm: DialogAction,
    message: String? = null,
    dismiss: DialogAction? = null,
) {
    Surface(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .padding(24.dp)
            .pointerInput(Unit) { detectTapGestures {} },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (message != null) {
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                if (dismiss != null) {
                    TextButton(onClick = dismiss.onClick) {
                        Text(text = dismiss.text, style = MaterialTheme.typography.labelLarge)
                    }
                }
                TextButton(onClick = confirm.onClick) {
                    Text(text = confirm.text, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
