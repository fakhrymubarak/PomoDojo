package com.fakhry.pomodojo.focus.domain.usecase

import android.content.Context
import android.media.MediaPlayer
import com.fakhry.pomodojo.R
import com.fakhry.pomodojo.focus.data.db.AndroidFocusDatabaseHolder

actual fun provideSegmentCompletionSoundPlayer(): SegmentCompletionSoundPlayer {
    val context = AndroidFocusDatabaseHolder.requireContext()
    return AndroidSegmentCompletionSoundPlayer(context.applicationContext)
}

private class AndroidSegmentCompletionSoundPlayer(
    private val context: Context,
) : SegmentCompletionSoundPlayer {
    override fun playSegmentCompleted() {
        val mediaPlayer = MediaPlayer.create(context, R.raw.timer_notification) ?: return
        mediaPlayer.setOnCompletionListener { player ->
            player.release()
        }
        mediaPlayer.start()
    }
}
