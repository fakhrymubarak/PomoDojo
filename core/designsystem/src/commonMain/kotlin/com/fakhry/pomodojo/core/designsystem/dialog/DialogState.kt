package com.fakhry.pomodojo.core.designsystem.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json

/**
 * Centralized controller for the single active dialog on a screen. Held in composition via
 * [rememberDialogState] and mutated with [showDialog] / [hideDialog]. Keeping one [current]
 * value prevents dialogs from overlapping unintentionally.
 */
@Stable
class DialogState {

    var current by mutableStateOf<DialogType>(DialogType.None)
        private set

    fun showDialog(dialog: DialogType) {
        current = dialog
    }

    fun hideDialog() {
        current = DialogType.None
    }

    companion object {
        /** Persists [current] as JSON so the active dialog survives process death (KMP-safe). */
        val Saver: Saver<DialogState, String> = Saver(
            save = { Json.encodeToString(DialogType.serializer(), it.current) },
            restore = { restored ->
                DialogState().apply {
                    current = Json.decodeFromString(DialogType.serializer(), restored)
                }
            },
        )
    }
}

@Composable
fun rememberDialogState(): DialogState =
    rememberSaveable(saver = DialogState.Saver) { DialogState() }
