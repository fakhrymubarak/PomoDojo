package com.fakhry.pomodojo.core.notification

interface SoundPlayer {
    fun playSegmentCompleted()
}

object NoOpSoundPlayer : SoundPlayer {
    override fun playSegmentCompleted() = Unit
}
