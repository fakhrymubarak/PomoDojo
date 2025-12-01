package com.fakhry.pomodojo.feature.notification.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.fakhry.pomodojo.feature.notification.R

actual fun provideSoundPlayer(): SoundPlayer = AndroidSoundPlayer

internal object AndroidSoundPlayer : SoundPlayer {
    private var appContext: Context? = null

    private var lastPlayCompletedSegment = 0L
    private const val DEBOUNCE_COMPLETED_AUDIO = 5_000L // 5 secs
    private const val TAG = "AndroidSegmentCompletionSoundPlayer"

    override fun playSegmentCompleted() {
        check(appContext != null) {
            "Android focus database not initialized. Call initAndroidFocusDatabase() first."
        }

        val now = System.currentTimeMillis()
        Log.d(TAG, "playSegment ${now - lastPlayCompletedSegment}")
        if (now - lastPlayCompletedSegment <= DEBOUNCE_COMPLETED_AUDIO) return
        val mediaPlayer = MediaPlayer.create(appContext, R.raw.timer_notification) ?: return
        mediaPlayer.setOnCompletionListener { player ->
            player.release()
        }
        mediaPlayer.start()
        lastPlayCompletedSegment = now
    }

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context
        }
    }

    fun destroy() {
        appContext = null
    }
}
