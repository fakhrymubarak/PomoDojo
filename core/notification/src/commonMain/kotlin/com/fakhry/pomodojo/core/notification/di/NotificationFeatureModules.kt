package com.fakhry.pomodojo.core.notification.di

import com.fakhry.pomodojo.core.notification.PomodoroSessionNotifier
import com.fakhry.pomodojo.core.notification.SoundPlayer
import com.fakhry.pomodojo.core.notification.audio.provideSoundPlayer
import com.fakhry.pomodojo.core.notification.notifications.providePomodoroSessionNotifier
import org.koin.dsl.module

val notificationCoreModule = module {
    single<PomodoroSessionNotifier> { providePomodoroSessionNotifier() }
    single<SoundPlayer> { provideSoundPlayer() }
}
