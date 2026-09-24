package com.fakhry.pomodojo.core.designsystem.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

/**
 * The "overlay" dialog type: [content] centered over a full-screen [backdrop], composited in the
 * current layout (not a separate window) so it can sit on top of on-screen effects such as the
 * edge glow.
 *
 * When [dismissable] is true, a tap on the backdrop (outside the content) invokes [onDismiss];
 * the content itself consumes taps (see [DialogCard]). When false, all pointer input is blocked
 * so the overlay is not skippable.
 */
@Composable
fun PomoModalOverlay(
    backdrop: ModalBackdrop,
    dismissable: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val pointerModifier = if (dismissable) {
        Modifier.pointerInput(Unit) { detectTapGestures { onDismiss() } }
    } else {
        // Consume every pointer event so the overlay blocks interaction and cannot be skipped.
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent()
                }
            }
        }
    }

    val rootModifier = modifier
        .fillMaxSize()
        .then(pointerModifier)

    when (backdrop) {
        is ModalBackdrop.Scrim -> Box(
            modifier = rootModifier.background(Color.Black.copy(alpha = backdrop.alpha)),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }

        is ModalBackdrop.EdgeGlow -> EdgeGlowOverlay(
            glowColor = backdrop.color,
            modifier = rootModifier,
            content = content,
        )
    }
}
