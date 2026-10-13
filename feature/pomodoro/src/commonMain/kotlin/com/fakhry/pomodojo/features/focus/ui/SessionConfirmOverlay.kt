package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.fakhry.pomodojo.core.designsystem.dialog.DialogAction
import com.fakhry.pomodojo.core.designsystem.dialog.DialogCard
import com.fakhry.pomodojo.core.designsystem.dialog.ModalBackdrop
import com.fakhry.pomodojo.core.designsystem.dialog.PomoModalOverlay
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_continue
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_end_message
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_end_title
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_finish
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_skip_confirm
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_skip_dismiss
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_skip_message
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_confirm_skip_title
import com.fakhry.pomodojo.features.focus.ui.model.SessionConfirm
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Asks the user to confirm [confirm]; tapping the scrim or the dismiss button answers "no". */
@Composable
internal fun SessionConfirmOverlay(
    confirm: SessionConfirm,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val texts = when (confirm) {
        SessionConfirm.END_SESSION -> ConfirmTexts(
            title = Res.string.focus_session_confirm_end_title,
            message = Res.string.focus_session_confirm_end_message,
            confirm = Res.string.focus_session_confirm_finish,
            dismiss = Res.string.focus_session_confirm_continue,
        )

        SessionConfirm.SKIP_BREAK -> ConfirmTexts(
            title = Res.string.focus_session_confirm_skip_title,
            message = Res.string.focus_session_confirm_skip_message,
            confirm = Res.string.focus_session_confirm_skip_confirm,
            dismiss = Res.string.focus_session_confirm_skip_dismiss,
        )
    }

    PomoModalOverlay(
        backdrop = ModalBackdrop.Scrim(),
        dismissable = true,
        onDismiss = onDismiss,
    ) {
        DialogCard(
            modifier = Modifier.testTag("session_confirm"),
            title = stringResource(texts.title),
            message = stringResource(texts.message),
            confirm = DialogAction(text = stringResource(texts.confirm), onClick = onConfirm),
            dismiss = DialogAction(text = stringResource(texts.dismiss), onClick = onDismiss),
        )
    }
}

private data class ConfirmTexts(
    val title: StringResource,
    val message: StringResource,
    val confirm: StringResource,
    val dismiss: StringResource,
)
