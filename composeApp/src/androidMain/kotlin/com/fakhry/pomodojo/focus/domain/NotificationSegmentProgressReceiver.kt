package com.fakhry.pomodojo.focus.domain

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.focus.data.db.createDatabase
import com.fakhry.pomodojo.focus.data.repository.ActiveSessionRepositoryImpl
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.usecase.provideSegmentCompletionSoundPlayer
import com.fakhry.pomodojo.preferences.domain.model.TimelineDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerSegmentsDomain
import com.fakhry.pomodojo.preferences.domain.model.TimerStatusDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "NotificationSegmentProgressReceiver"

/**
 * BroadcastReceiver that handles segment completion alarms.
 * Triggered by AlarmManager when a pomodoro segment is expected to complete.
 */
class NotificationSegmentProgressReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION_SEGMENT_COMPLETE && action != ACTION_PROGRESS_UPDATE) return

        // Check notification permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val pendingResult = goAsync()

        scope.launch {
            try {
                // Initialize dependencies
                val database = createDatabase()
                val sessionRepository = ActiveSessionRepositoryImpl(database, DispatcherProvider())
                val notifier = provideFocusSessionNotifier()

                // Check if there's an active session
                val hasSession = sessionRepository.hasActiveSession()
                if (!hasSession) {
                    pendingResult.finish()
                    return@launch
                }

                // Get the current session and process segment completion
                val session = sessionRepository.getActiveSession()
                val now = System.currentTimeMillis()

                when (action) {
                    ACTION_SEGMENT_COMPLETE -> {
                        // Process the session to advance segments if current one is completed
                        val updatedSession = processSessionCompletion(session, now)

                        // Update the session in repository if it was modified
                        if (updatedSession != session) {
                            Log.i(TAG, "onReceive: session updated")
                            sessionRepository.updateActiveSession(updatedSession)
                        }

                        // Schedule notification with the updated session
                        notifier.schedule(updatedSession)
                        provideSegmentCompletionSoundPlayer().playSegmentCompleted()
                    }

                    ACTION_PROGRESS_UPDATE -> {
                        // Just refresh the notification to update progress bar
                        Log.i(TAG, "onReceive: progress updated")
                        notifier.schedule(session)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "Failed to schedule segment completion alarm")
                e.printStackTrace()
            } finally {
                Log.i(TAG, "onReceive: finally finish pending result")
                pendingResult.finish()
            }
        }
    }

    companion object Companion {
        const val ACTION_SEGMENT_COMPLETE = "com.fakhry.pomodojo.SEGMENT_COMPLETE"
        const val ACTION_PROGRESS_UPDATE = "com.fakhry.pomodojo.PROGRESS_UPDATE"
        const val EXTRA_SESSION_ID = "session_id"
    }
}

/**
 * Processes the session to check if the current segment is complete and advances to the next segment if needed.
 * Loops through all overdue segments to catch up when alarms fire late.
 * Returns the updated session if any changes were made, or the original session if no changes.
 */
private fun processSessionCompletion(
    session: PomodoroSessionDomain,
    now: Long,
): PomodoroSessionDomain {
    val segments = session.timeline.segments.toMutableList()
    if (segments.isEmpty()) return session

    // Find the active segment index
    var activeIndex = segments.indexOfFirst {
        it.timerStatus == TimerStatusDomain.RUNNING || it.timerStatus == TimerStatusDomain.PAUSED
    }
    if (activeIndex == -1) {
        activeIndex = segments.indexOfFirst { it.timerStatus != TimerStatusDomain.COMPLETED }
    }
    if (activeIndex == -1) return session // All segments completed

    var modified = false
    var currentIndex = activeIndex

    // Loop through all overdue segments until we find one that's still in the future
    while (currentIndex <= segments.lastIndex) {
        val currentSegment = segments[currentIndex]

        val shouldAdvance = when (currentSegment.timerStatus) {
            TimerStatusDomain.RUNNING -> {
                val remaining = (currentSegment.timer.finishedInMillis - now).coerceAtLeast(0L)
                if (remaining == 0L) {
                    Log.i(
                        TAG,
                        "processSessionCompletion: segment $currentIndex completed, advancing to next",
                    )
                    segments[currentIndex] = finalizeSegment(currentSegment)
                    modified = true
                    true
                } else {
                    false
                }
            }

            TimerStatusDomain.COMPLETED -> {
                Log.i(
                    TAG,
                    "processSessionCompletion: segment $currentIndex already completed, advancing to next",
                )
                true
            }

            else -> false
        }

        if (!shouldAdvance || currentIndex >= segments.lastIndex) break

        // Try to advance to the next segment
        val nextStartAt = currentSegment.timer.finishedInMillis.takeIf { it > 0L } ?: now
        val nextSegment = prepareSegmentForRun(
            segments[currentIndex + 1],
            startedAt = nextStartAt,
            referenceTime = now,
        )
        segments[currentIndex + 1] = nextSegment
        modified = true
        Log.i(TAG, "processSessionCompletion: started segment ${currentIndex + 1}")

        // If the next segment is already completed (overdue), continue the loop
        if (nextSegment.timerStatus == TimerStatusDomain.COMPLETED) {
            currentIndex++
            continue
        } else {
            // Found a segment that's still running, we're done
            break
        }
    }

    return if (modified) {
        session.copy(
            timeline = TimelineDomain(
                segments = segments,
                hourSplits = session.timeline.hourSplits,
            ),
        )
    } else {
        session
    }
}

/**
 * Finalizes a segment by setting its status to COMPLETED and clamping progress to 1.0
 */
private fun finalizeSegment(segment: TimerSegmentsDomain): TimerSegmentsDomain {
    val timer = segment.timer.copy(
        progress = segment.timer.progress.coerceAtMost(1f),
        startedPauseTime = 0L,
    )
    return segment.copy(timer = timer, timerStatus = TimerStatusDomain.COMPLETED)
}

/**
 * Prepares a segment to start running by calculating its finish time and setting status to RUNNING
 */
private fun prepareSegmentForRun(
    segment: TimerSegmentsDomain,
    startedAt: Long,
    referenceTime: Long,
): TimerSegmentsDomain {
    val duration = segment.timer.durationEpochMs
    val start = startedAt.takeIf { it > 0L } ?: referenceTime
    val finishedAt = start + duration
    val remaining = (finishedAt - referenceTime).coerceAtLeast(0L)
    val progress = if (duration > 0L) {
        (duration - remaining).toFloat() / duration
    } else {
        0f
    }
    val timer = segment.timer.copy(
        progress = progress,
        finishedInMillis = finishedAt,
        startedPauseTime = 0L,
        elapsedPauseTime = 0L,
    )
    val status = if (remaining == 0L) TimerStatusDomain.COMPLETED else TimerStatusDomain.RUNNING
    return segment.copy(timer = timer, timerStatus = status)
}
