package com.fakhry.pomodojo.core.framework.notifications

actual fun providePomodoroSessionNotifier(): PomodoroSessionNotifier = NoOpPomodoroSessionNotifier
