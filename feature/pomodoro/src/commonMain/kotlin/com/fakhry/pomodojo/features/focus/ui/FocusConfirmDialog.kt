package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_continue
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_end_message
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_end_title
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_finish
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FocusConfirmDialog(onConfirmFinish: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.focus_session_confirm_end_title),
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.focus_session_confirm_end_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmFinish) {
                Text(
                    text = stringResource(Res.string.focus_session_confirm_finish),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.focus_session_confirm_continue),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}
