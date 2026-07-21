package com.fakhry.pomodojo.core.designsystem.dialog

import androidx.compose.ui.graphics.Color

/** Backdrop drawn behind an overlay modal's content in [PomoModalOverlay]. */
sealed interface ModalBackdrop {
    /** A flat dimming scrim. */
    data class Scrim(val alpha: Float = 0.6f) : ModalBackdrop

    /** The [EdgeGlowOverlay] glow, tinted by [color], optionally with the intro sweep. */
    data class EdgeGlow(val color: Color, val animateIntro: Boolean) : ModalBackdrop
}
