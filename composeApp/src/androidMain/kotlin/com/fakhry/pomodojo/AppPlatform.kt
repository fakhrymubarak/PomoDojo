package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fakhry.pomodojo.preferences.initAndroidPreferenceStorage

@Composable
internal actual fun PlatformKoinInitializer() {
    val context = LocalContext.current.applicationContext
    remember(context) {
        initAndroidPreferenceStorage(context)
        Unit
    }
}
