package com.fakhry.pomodojo.core.designsystem.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fakhry.pomodojo.core.designsystem.theme.PomoDojoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The "window" dialog type: a real platform dialog window (via [Dialog]) wrapping the shared
 * [DialogCard]. Use for dialogs that should float in their own window above everything. For a
 * dialog that must composite with on-screen content (e.g. a glow backdrop), use
 * [PomoModalOverlay] instead.
 */
@Composable
fun PomoDialog(
    title: String,
    confirm: DialogAction,
    onDismissRequest: () -> Unit,
    message: String? = null,
    dismiss: DialogAction? = null,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
        ),
    ) {
        DialogCard(
            title = title,
            confirm = confirm,
            message = message,
            dismiss = dismiss,
        )
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
