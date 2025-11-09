package com.fakhry.pomodojo.preferences.ui

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
    const val Screen = "PreferencesScreen"
    const val Content = "PreferencesContent"
    const val TimelinePreview = "PomodoroTimelinePreviewSection"
    const val ConfigSection = "PomodoroConfigSection"
    const val RepeatSection = "RepeatSection"
    const val AppearanceSection = "PreferenceAppearanceSection"
}
