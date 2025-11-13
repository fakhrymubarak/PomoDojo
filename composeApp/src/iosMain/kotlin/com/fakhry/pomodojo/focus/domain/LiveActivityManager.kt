package com.fakhry.pomodojo.focus.domain

import com.fakhry.pomodojo.liveactivity.bridge.PomodoroLiveActivityBridge
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * LiveActivityManager - Kotlin wrapper for PomodoroLiveActivityBridge
 *
 * Calls the Objective-C bridge which then calls Swift Live Activity code.
 * Uses cinterop-generated bindings to call Objective-C methods.
 */
@OptIn(ExperimentalForeignApi::class)
object LiveActivityManager {

    fun isSupported(): Boolean = try {
        val supported = PomodoroLiveActivityBridge.isSupported()
        println("LiveActivityManager: isSupported = $supported")
        supported
    } catch (e: Exception) {
        println("LiveActivityManager: isSupported failed: ${e.message}")
        false
    }

    fun startLiveActivity(
        sessionId: String,
        quote: String,
        cycleNumber: Int,
        totalCycles: Int,
        segmentType: String,
        remainingSeconds: Int,
        totalSeconds: Int,
        isPaused: Boolean,
    ) {
        try {
            println("LiveActivityManager: Starting Live Activity")
            println("  Session: $sessionId")
            println("  Cycle: $cycleNumber/$totalCycles")
            println("  Segment: $segmentType")
            println("  Remaining: $remainingSeconds seconds")

            val bridge = PomodoroLiveActivityBridge.shared()

            bridge?.startLiveActivityWithSessionId(
                sessionId = sessionId,
                quote = quote,
                cycleNumber = cycleNumber.toLong(),
                totalCycles = totalCycles.toLong(),
                segmentType = segmentType,
                remainingSeconds = remainingSeconds.toLong(),
                totalSeconds = totalSeconds.toLong(),
                isPaused = isPaused,
            )

            println("LiveActivityManager: Live Activity started successfully")
        } catch (e: Exception) {
            println("LiveActivityManager: Failed to start: ${e.message}")
            e.printStackTrace()
        }
    }

    fun updateLiveActivity(
        cycleNumber: Int,
        totalCycles: Int,
        segmentType: String,
        remainingSeconds: Int,
        totalSeconds: Int,
        isPaused: Boolean,
    ) {
        try {
            println("LiveActivityManager: Updating Live Activity")
            println("  Cycle: $cycleNumber/$totalCycles")
            println("  Segment: $segmentType")

            val bridge = PomodoroLiveActivityBridge.shared()

            bridge?.updateLiveActivityWithCycleNumber(
                cycleNumber = cycleNumber.toLong(),
                totalCycles = totalCycles.toLong(),
                segmentType = segmentType,
                remainingSeconds = remainingSeconds.toLong(),
                totalSeconds = totalSeconds.toLong(),
                isPaused = isPaused,
            )
        } catch (e: Exception) {
            println("LiveActivityManager: Failed to update: ${e.message}")
        }
    }

    fun endLiveActivity() {
        try {
            println("LiveActivityManager: Ending Live Activity")

            val bridge = PomodoroLiveActivityBridge.shared()
            bridge?.endLiveActivity()
        } catch (e: Exception) {
            println("LiveActivityManager: Failed to end: ${e.message}")
        }
    }

    fun endLiveActivityWithCompletion(
        completedCycles: Int,
        totalFocusMinutes: Int,
        totalBreakMinutes: Int,
    ) {
        try {
            println("LiveActivityManager: Ending with completion")
            println("  Cycles: $completedCycles")
            println("  Focus: $totalFocusMinutes min")
            println("  Break: $totalBreakMinutes min")

            val bridge = PomodoroLiveActivityBridge.shared()

            bridge?.endLiveActivityWithCompletionCycles(
                completedCycles = completedCycles.toLong(),
                totalFocusMinutes = totalFocusMinutes.toLong(),
                totalBreakMinutes = totalBreakMinutes.toLong(),
            )
        } catch (e: Exception) {
            println("LiveActivityManager: Failed to end with completion: ${e.message}")
        }
    }
}
