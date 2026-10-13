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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.designsystem.theme.PomoDojoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The shared visual card for overlay modals ([PomoModalOverlay]). No window and no scrim of its
 * own — just the titled card with an optional [message] and [confirm] / [dismiss] actions.
 *
 * The card consumes tap gestures so a dismiss-on-scrim overlay ignores taps that land on it.
 */
@Composable
fun DialogCard(
    title: String,
    confirm: DialogAction,
    message: String? = null,
    dismiss: DialogAction? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
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
                    TextButton(
                        onClick = dismiss.onClick,
                        modifier = Modifier.testTag("dialog_dismiss"),
                    ) {
                        Text(text = dismiss.text, style = MaterialTheme.typography.labelLarge)
                    }
                }
                TextButton(
                    onClick = confirm.onClick,
                    modifier = Modifier.testTag("dialog_confirm"),
                ) {
                    Text(text = confirm.text, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview
@Composable
fun PomoDialogCardPreview() {
    PomoDojoTheme {
        DialogCard(
            title = "End Focus?",
            message = "Are you sure you want to end this pomodoro?",
            confirm = DialogAction(text = "Finish Pomodoro", onClick = {}),
            dismiss = DialogAction(text = "Continue Focus", onClick = {}),
        )
    }
}
