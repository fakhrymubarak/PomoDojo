package com.fakhry.pomodojo.core.designsystem.dialog

/** A dialog button: its [text] and the [onClick] it triggers. */
data class DialogAction(
    val text: String,
    val onClick: () -> Unit,
)
