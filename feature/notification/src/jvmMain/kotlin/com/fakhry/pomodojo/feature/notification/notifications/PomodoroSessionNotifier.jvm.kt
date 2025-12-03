package com.fakhry.pomodojo.feature.notification.notifications

import com.fakhry.pomodojo.core.notification.NoOpPomodoroSessionNotifier
import com.fakhry.pomodojo.core.notification.PomodoroSessionNotifier

actual fun providePomodoroSessionNotifier(): PomodoroSessionNotifier = NoOpPomodoroSessionNotifier
