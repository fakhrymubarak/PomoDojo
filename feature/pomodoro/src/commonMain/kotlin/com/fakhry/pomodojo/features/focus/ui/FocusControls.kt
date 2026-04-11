package com.fakhry.pomodojo.features.focus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.designsystem.generated.resources.Res
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_end_content_description
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_pause_content_description
import com.fakhry.pomodojo.core.designsystem.generated.resources.focus_session_resume_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FocusControls(
    isTimerRunning: Boolean,
    onTogglePause: () -> Unit = {},
    onEnd: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val icon = if (isTimerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
        val description = if (isTimerRunning) {
            stringResource(Res.string.focus_session_pause_content_description)
        } else {
            stringResource(Res.string.focus_session_resume_content_description)
        }
        FocusCircularButton(
            onClick = onTogglePause,
            icon = { Icon(imageVector = icon, contentDescription = null) },
            buttonDescription = description,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )

        FocusCircularButton(
            onClick = onEnd,
            icon = { Icon(imageVector = Icons.Rounded.Close, contentDescription = null) },
            buttonDescription = stringResource(Res.string.focus_session_end_content_description),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        )
    }
}

@Composable
internal fun FocusCircularButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    buttonDescription: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        modifier = Modifier.size(64.dp).semantics { this.contentDescription = buttonDescription },
        shape = CircleShape,
        color = containerColor,
        onClick = onClick,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
        }
    }
}
