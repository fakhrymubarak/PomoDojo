package com.fakhry.pomodojo.core.framework.audio

interface SoundPlayer {
    fun playSegmentCompleted()
}

expect fun provideSoundPlayer(): SoundPlayer
