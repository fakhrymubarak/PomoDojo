package com.fakhry.pomodojo.focus.domain.usecase

interface SegmentCompletionSoundPlayer {
    fun playSegmentCompleted()
}

expect fun provideSegmentCompletionSoundPlayer(): SegmentCompletionSoundPlayer
