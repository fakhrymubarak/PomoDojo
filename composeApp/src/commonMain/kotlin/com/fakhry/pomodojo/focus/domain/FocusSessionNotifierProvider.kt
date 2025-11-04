package com.fakhry.pomodojo.focus.domain

import com.fakhry.pomodojo.focus.domain.usecase.FocusSessionNotifier

expect fun provideFocusSessionNotifier(): FocusSessionNotifier
