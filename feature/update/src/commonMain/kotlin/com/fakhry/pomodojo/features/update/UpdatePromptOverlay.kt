package com.fakhry.pomodojo.features.update

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.testTag
import com.fakhry.pomodojo.core.designsystem.dialog.DialogAction
import com.fakhry.pomodojo.core.designsystem.dialog.DialogCard
import com.fakhry.pomodojo.core.designsystem.dialog.ModalBackdrop
import com.fakhry.pomodojo.core.designsystem.dialog.PomoModalOverlay
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.app_update_force_message
import com.fakhry.pomodojo.core.designsystem.generated.resources.app_update_force_title
import com.fakhry.pomodojo.core.designsystem.generated.resources.app_update_later
import com.fakhry.pomodojo.core.designsystem.generated.resources.app_update_ready_message
import com.fakhry.pomodojo.core.designsystem.generated.resources.app_update_ready_title
import com.fakhry.pomodojo.core.designsystem.generated.resources.app_update_restart
import com.fakhry.pomodojo.core.designsystem.generated.resources.app_update_soft_message
import com.fakhry.pomodojo.core.designsystem.generated.resources.app_update_soft_title
import com.fakhry.pomodojo.core.designsystem.generated.resources.app_update_update
import org.jetbrains.compose.resources.stringResource

/** Shows [prompt]; [onConfirm] updates or restarts, [onLater] declines (never for [UpdatePrompt.FORCE]). */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun UpdatePromptOverlay(
    prompt: UpdatePrompt,
    onConfirm: () -> Unit,
    onLater: () -> Unit,
) {
    val force = prompt == UpdatePrompt.FORCE
    // Swallow back so a force update cannot be skipped by navigating underneath it.
    BackHandler(enabled = force) {}

    PomoModalOverlay(
        backdrop = ModalBackdrop.Scrim(),
        dismissable = !force,
        onDismiss = onLater,
    ) {
        val later =
            DialogAction(text = stringResource(Res.string.app_update_later), onClick = onLater)
        when (prompt) {
            UpdatePrompt.FORCE -> DialogCard(
                modifier = Modifier.testTag("app_update_force"),
                title = stringResource(Res.string.app_update_force_title),
                message = stringResource(Res.string.app_update_force_message),
                confirm = DialogAction(
                    text = stringResource(Res.string.app_update_update),
                    onClick = onConfirm,
                ),
            )

            UpdatePrompt.SOFT -> DialogCard(
                modifier = Modifier.testTag("app_update_soft"),
                title = stringResource(Res.string.app_update_soft_title),
                message = stringResource(Res.string.app_update_soft_message),
                confirm = DialogAction(
                    text = stringResource(Res.string.app_update_update),
                    onClick = onConfirm,
                ),
                dismiss = later,
            )

            UpdatePrompt.RESTART -> DialogCard(
                modifier = Modifier.testTag("app_update_restart"),
                title = stringResource(Res.string.app_update_ready_title),
                message = stringResource(Res.string.app_update_ready_message),
                confirm = DialogAction(
                    text = stringResource(Res.string.app_update_restart),
                    onClick = onConfirm,
                ),
                dismiss = later,
            )
        }
    }
}
