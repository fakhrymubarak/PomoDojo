package com.fakhry.pomodojo.core.designsystem.dialog

import kotlinx.serialization.Serializable

/**
 * Serializable description of the currently active dialog.
 *
 * Variants carry only serializable discriminators (no lambdas, colors, or strings) so the
 * active dialog survives process death via [DialogState.Saver]. The text and action for each
 * variant are resolved at render time in [DialogHandler].
 */
@Serializable
sealed interface DialogType {

    /** No dialog is shown. */
    @Serializable
    data object None : DialogType

    /** A confirm dialog rendered as a scrim-backed overlay modal. */
    @Serializable
    data class Confirm(val kind: ConfirmKind) : DialogType

    /** A plain-scrim overlay modal. */
    @Serializable
    data class Modal(val type: ModalType) : DialogType
}

/** Discriminator that maps a [DialogType.Confirm] to its text and confirm action. */
enum class ConfirmKind {
    END_FOCUS_SESSION,
    SKIP_BREAK,
}

/** Discriminator that maps a [DialogType.Modal] to its content. */
enum class ModalType {
    EXAMPLE,
}
