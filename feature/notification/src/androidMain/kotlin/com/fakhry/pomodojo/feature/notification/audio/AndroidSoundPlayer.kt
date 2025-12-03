package com.fakhry.pomodojo.feature.notification.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.fakhry.pomodojo.core.notification.SoundPlayer
import com.fakhry.pomodojo.feature.notification.R
import org.koin.core.context.GlobalContext

actual fun provideSoundPlayer(): SoundPlayer {
    val koin = GlobalContext.get()
    return koin.get<AndroidSoundPlayer>()
}

private const val DEBOUNCE_COMPLETED_AUDIO = 5_000L // 5 secs
private const val TAG = "AndroidSegmentCompletionSoundPlayer"

class AndroidSoundPlayer(private var appContext: Context) : SoundPlayer {
    private var lastPlayCompletedSegment = 0L

    override fun playSegmentCompleted() {
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
}
