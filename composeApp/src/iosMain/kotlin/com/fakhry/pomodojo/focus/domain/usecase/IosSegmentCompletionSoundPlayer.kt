package com.fakhry.pomodojo.focus.domain.usecase

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

actual fun provideSegmentCompletionSoundPlayer(): SegmentCompletionSoundPlayer =
    IosSegmentCompletionSoundPlayer()

private class IosSegmentCompletionSoundPlayer : SegmentCompletionSoundPlayer {
    private val player: AVAudioPlayer? by lazy {
        configureAudioSession()
        loadPlayer()
    }

    override fun playSegmentCompleted() {
        player?.let {
            it.stop()
            it.currentTime = 0.0
            it.play()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun loadPlayer(): AVAudioPlayer? {
        val path = resolveSoundPath() ?: return null
        val url = NSURL.fileURLWithPath(path = path)
        val audioPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
        audioPlayer.prepareToPlay()
        return audioPlayer
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun configureAudioSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryAmbient,
            withOptions = AVAudioSessionCategoryOptionMixWithOthers,
            error = null,
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun resolveSoundPath(): String? {
        val bundle = NSBundle.mainBundle
        val soundName = "timer_notification"
        val soundType = "wav"

        val directPath = bundle.pathForResource(soundName, soundType)
        if (directPath != null) return directPath

        val composeResourcesPath =
            bundle.pathForResource(soundName, soundType, "compose-resources")
        if (composeResourcesPath != null) return composeResourcesPath

        val nestedComposeResourcesPath =
            bundle.pathForResource(soundName, soundType, "compose-resources/composeResources")
        if (nestedComposeResourcesPath != null) return nestedComposeResourcesPath

        val bundleResourcePath =
            bundle.resourcePath?.let { "$it/compose-resources/$soundName.$soundType" }
        if (
            bundleResourcePath != null &&
            NSFileManager.defaultManager.fileExistsAtPath(bundleResourcePath)
        ) {
            return bundleResourcePath
        }

        return null
    }
}
