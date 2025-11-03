package com.fakhry.pomodojo.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PomoDojo Color System
 * Based on themes-specs.md
 */

// Surface Colors
val LightSurface = Color(0xFFFF6C6C)
val DarkSurface = Color(0xFF1A1A1A) // Dark background

// Primary Color (Tomato/Red)
val Primary = Color(0xFFBF4A35)

// Secondary Color (Green)
val Secondary = Color(0xFF567D41)
val OnSecondary = Color(0xFFE9F9F9)

// Background Colors
val DarkBackground = Color(0xFF1A1A1A)
val DarkCircleBackground = Color(0xFF3A3A3A)

// Text Colors
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
val ButtonPrimary = Primary
val ButtonSecondary = Color.Transparent
val BorderDefault = Color(0xFFCCCCCC)
val BorderDark = Color(0xFF333333)

// Toggle/Slider Colors
val SliderTrack = Color(0xFF333333)
val SliderActive = Secondary
val ToggleActive = Primary
val ToggleInactive = Color(0xFF333333)

// Overlay Colors
val PausedOverlay = Secondary.copy(alpha = 0.7f)

// Dialog/Modal Colors
val DialogBackground = Color(0xFF2A2A2A)
val DialogBackgroundAlt = Color(0xFF333333)
