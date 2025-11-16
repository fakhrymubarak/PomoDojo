package com.fakhry.pomodojo.utils.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ColumnScope.Expanded() {
    Spacer(modifier = Modifier.weight(1f).fillMaxWidth())
}
