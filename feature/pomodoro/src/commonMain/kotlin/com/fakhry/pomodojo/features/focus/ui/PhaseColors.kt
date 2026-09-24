package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.ui.graphics.Color
import com.fakhry.pomodojo.core.designsystem.model.TimerTypeUi
import com.fakhry.pomodojo.core.designsystem.theme.LongBreakHighlight
import com.fakhry.pomodojo.core.designsystem.theme.Primary
import com.fakhry.pomodojo.core.designsystem.theme.Secondary

/** Glow color used to tint a phase's transition gate and end-session confirm overlay. */
internal fun TimerTypeUi.phaseGlowColor(): Color = when (this) {
    TimerTypeUi.FOCUS -> Secondary
    TimerTypeUi.SHORT_BREAK -> Primary
    TimerTypeUi.LONG_BREAK -> LongBreakHighlight
}
