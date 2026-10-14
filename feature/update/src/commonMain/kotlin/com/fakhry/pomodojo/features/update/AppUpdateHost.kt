package com.fakhry.pomodojo.features.update

import androidx.compose.runtime.Composable

/**
 * Checks Remote Config once per launch and prompts for a force or soft update once no Session is
 * active. Place it above the app content so its overlays cover everything.
 */
@Composable
expect fun AppUpdateHost()
