package com.fakhry.pomodojo.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PomoDojo color system tokens used to build light and dark schemes.
 */

// Brand colors
val Primary = Color(0xFFBF4A35)
val Secondary = Color(0xFF567D41)

// Dark theme neutrals
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2A2A2A)
val DarkCircleBackground = Color(0xFF3A3A3A)
val DarkOnPrimary = Color(0xFFE9F9F9)
val DarkOnSurface = Color(0xFFE9F9F9)
val DarkOnSurfaceVariant = Color(0xFFCCCCCC)
val DarkOutline = Color(0xFF404040)

// Light theme neutrals
val LightBackground = Color(0xFFF7F7F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F0F0)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF1F1F1F)
val LightOnSurface = Color(0xFF1F1F1F)
val LightOnSurfaceVariant = Color(0xFF5C5C5C)
val LightOutline = Color(0xFFDDDDDD)

// Text helpers
val TextWhite = Color(0xFFE9F9F9)
val TextLightGray = Color(0xFFCCCCCC)
val TextDarkGray = Color(0xFF333333)

// Graph Color Levels (7-level intensity system)
val GraphLevel0 = Color(0xffb9b9b9) // No activity (light gray)
val GraphLevel1 = Color(0xFF7BB35D) // 1-16 minutes
val GraphLevel2 = Color(0xFF6FA054) // 17-33 minutes
val GraphLevel3 = Color(0xFF638E49) // 34-50 minutes
val GraphLevel4 = Color(0xFF577D41) // 51-67 minutes
val GraphLevel5 = Color(0xFF496B38) // 68-84 minutes
val GraphLevel6 = Color(0xFF3E5A2F) // 85-100 minutes

// Component Colors
val ButtonSecondary = Color.Transparent
val PausedOverlay = Secondary.copy(alpha = 0.7f)
