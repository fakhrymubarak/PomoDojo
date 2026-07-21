package com.fakhry.pomodojo.core.designsystem.dialog

import androidx.compose.runtime.Composable
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_continue
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_end_message
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_end_title
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_finish
import org.jetbrains.compose.resources.stringResource

/**
 * Renders the active dialog from [state]. Call it inside the screen's root `Box` (after the
 * content) so overlay modals layer on top.
 *
 * Because [DialogType] payloads are serializable discriminators, the text/action are resolved
 * here: [onConfirm] receives the [ConfirmKind] to run the matching action, [onDismiss] handles
 * cancellation (button or scrim tap).
 */
@Composable
fun DialogHandler(state: DialogState, onConfirm: (ConfirmKind) -> Unit, onDismiss: () -> Unit) {
    when (val dialog = state.current) {
        is DialogType.None -> Unit

        is DialogType.Confirm -> ConfirmOverlay(
            kind = dialog.kind,
            onConfirm = { onConfirm(dialog.kind) },
            onDismiss = onDismiss,
        )

        is DialogType.Modal -> PomoModalOverlay(
            backdrop = ModalBackdrop.Scrim(),
            dismissable = true,
            onDismiss = onDismiss,
        ) {
            ModalContent(dialog.type)
        }
    }
}

@Composable
private fun ConfirmOverlay(kind: ConfirmKind, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val texts = when (kind) {
        ConfirmKind.END_FOCUS_SESSION -> ConfirmTexts(
            title = stringResource(Res.string.focus_session_confirm_end_title),
            message = stringResource(Res.string.focus_session_confirm_end_message),
            confirmText = stringResource(Res.string.focus_session_confirm_finish),
            dismissText = stringResource(Res.string.focus_session_confirm_continue),
        )
    }

    PomoModalOverlay(
        backdrop = ModalBackdrop.Scrim(),
        dismissable = true,
        onDismiss = onDismiss,
    ) {
        DialogCard(
            title = texts.title,
            message = texts.message,
            confirm = DialogAction(text = texts.confirmText, onClick = onConfirm),
            dismiss = DialogAction(text = texts.dismissText, onClick = onDismiss),
        )
    }
}

/** Demo content for the plain-scrim [DialogType.Modal] overlay (no real usage yet). */
@Composable
private fun ModalContent(type: ModalType) {
    when (type) {
        ModalType.EXAMPLE -> DialogCard(
            title = "Example modal",
            message = "A plain-scrim overlay modal.",
            confirm = DialogAction(text = "OK", onClick = {}),
        )
    }
}

private data class ConfirmTexts(
    val title: String,
    val message: String,
    val confirmText: String,
    val dismissText: String,
)
