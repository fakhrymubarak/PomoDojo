package com.fakhry.pomodojo.feature.notification.audio

interface SoundPlayer {
    fun playSegmentCompleted()
}

expect fun provideSoundPlayer(): SoundPlayer
