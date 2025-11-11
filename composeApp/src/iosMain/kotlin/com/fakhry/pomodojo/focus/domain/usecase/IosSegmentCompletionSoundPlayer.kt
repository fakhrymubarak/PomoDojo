package com.fakhry.pomodojo.focus.domain.usecase

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

actual fun provideSegmentCompletionSoundPlayer(): SegmentCompletionSoundPlayer =
    IosSegmentCompletionSoundPlayer()

private class IosSegmentCompletionSoundPlayer : SegmentCompletionSoundPlayer {
    private val player: AVAudioPlayer? = loadPlayer()

    override fun playSegmentCompleted() {
        player?.let {
            it.stop()
            it.currentTime = 0.0
            it.play()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun loadPlayer(): AVAudioPlayer? {
        val path = NSBundle.mainBundle.pathForResource("timer_notification", "wav") ?: return null
        val url = NSURL.fileURLWithPath(path = path)
        val audioPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
        audioPlayer.prepareToPlay()
        return audioPlayer
    }
}
