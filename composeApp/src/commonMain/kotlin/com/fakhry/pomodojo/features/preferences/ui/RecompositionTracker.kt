package com.fakhry.pomodojo.features.preferences.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf

fun interface RecompositionObserver {
    fun onRecompose(tag: String)
}

private val DefaultObserver = RecompositionObserver {}

val LocalRecompositionObserver = staticCompositionLocalOf { DefaultObserver }

@Composable
fun TrackRecomposition(tag: String) {
    val observer = LocalRecompositionObserver.current
    if (observer === DefaultObserver) return
    SideEffect {
        observer.onRecompose(tag)
    }
}

object RecompositionTags {
    const val SCREEN = "PreferencesScreen"
    const val CONTENT = "PreferencesContent"
    const val TIMELINE_PREVIEW = "PomodoroTimelinePreviewSection"
    const val CONFIG_SECTION = "PomodoroConfigSection"
    const val REPEAT_SECTION = "RepeatSection"
    const val APPEARANCE_SECTION = "PreferenceAppearanceSection"
}
