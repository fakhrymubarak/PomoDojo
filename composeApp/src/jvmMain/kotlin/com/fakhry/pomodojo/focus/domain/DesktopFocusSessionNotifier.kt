package com.fakhry.pomodojo.focus.domain

import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier
import com.fakhry.pomodojo.focus.domain.usecase.NoOpFocusSessionNotifier

actual fun provideFocusSessionNotifier(): FocusSessionNotifier = NoOpFocusSessionNotifier
