package com.fakhry.pomodojo.feature.notification.audio

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

actual fun provideSoundPlayer(): SoundPlayer = DesktopSoundPlayer()

private class DesktopSoundPlayer : SoundPlayer {
    private val clip: Clip? = loadClip()

    override fun playSegmentCompleted() {
        clip?.let {
            runCatching {
                if (it.isRunning) {
                    it.stop()
                }
                it.framePosition = 0
                it.start()
            }
        }
    }

    private fun loadClip(): Clip? {
        val resource = javaClass.getResource("/timer_notification.wav") ?: return null
        return runCatching {
            AudioSystem.getAudioInputStream(resource).use { audioStream ->
                AudioSystem.getClip().apply {
                    open(audioStream)
                }
            }
        }.getOrNull()
    }
}
