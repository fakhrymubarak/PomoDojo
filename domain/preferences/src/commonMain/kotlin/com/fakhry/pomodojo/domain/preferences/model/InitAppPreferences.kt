package com.fakhry.pomodojo.domain.preferences.model

data class InitAppPreferences(
    val appTheme: AppTheme = AppTheme.DARK,
    val hasActiveSession: Boolean = false,
)

/**
 * Supported application appearance themes.
 */
enum class AppTheme(val storageValue: String, val displayName: String) {
    DARK(storageValue = "dark", displayName = "🌘Dark"),
    LIGHT(storageValue = "light", displayName = "☀️Light"),
    ;

    companion object {
        fun fromStorage(value: String?): AppTheme =
            entries.firstOrNull { it.storageValue == value } ?: DARK
    }
}
