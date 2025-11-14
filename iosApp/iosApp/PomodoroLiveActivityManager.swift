import ActivityKit
import Foundation

@available(iOS 16.2, *)
@objc public class PomodoroLiveActivityManager: NSObject {
    private var currentActivity: Activity<PomodoroActivityAttributes>?
    private var currentSessionId: String?
    private var currentQuote: String?

    @objc public static let shared = PomodoroLiveActivityManager()

    private override init() {
        super.init()
    }

    // Start a new Live Activity
    @objc public func startLiveActivity(
        sessionId: String,
        quote: String,
        cycleNumber: Int,
        totalCycles: Int,
        segmentType: String,
        remainingSeconds: Int,
        totalSeconds: Int,
        isPaused: Bool
    ) {
        print("🚀 PomodoroLiveActivityManager: startLiveActivity called")
        print("   Session: \(sessionId)")
        print("   Quote: \(quote.prefix(30))...")
        print("   Cycle: \(cycleNumber)/\(totalCycles)")
        print("   Segment: \(segmentType)")
        print("   Remaining: \(remainingSeconds)s")
        print("   Paused: \(isPaused)")

        // End any existing activity first
        endLiveActivity()

        // Store session info for potential restarts
        currentSessionId = sessionId
        currentQuote = quote

        let attributes = PomodoroActivityAttributes(
            sessionId: sessionId,
            quote: quote
        )

        let targetEndTime = isPaused ? Date() : Date().addingTimeInterval(TimeInterval(remainingSeconds))

        let contentState = PomodoroActivityAttributes.ContentState(
            cycleNumber: cycleNumber,
            totalCycles: totalCycles,
            segmentType: segmentType,
            remainingSeconds: remainingSeconds,
            totalSeconds: totalSeconds,
            isPaused: isPaused,
            targetEndTime: targetEndTime
        )

        do {
            currentActivity = try Activity.request(
                attributes: attributes,
                content: .init(state: contentState, staleDate: nil),
                pushType: nil
            )
            print("✅ PomodoroLiveActivityManager: Live Activity started successfully!")
            print("   Activity ID: \(currentActivity?.id ?? "nil")")
        } catch {
            print("❌ PomodoroLiveActivityManager: Failed to start Live Activity")
            print("   Error: \(error.localizedDescription)")
            print("   Full error: \(error)")
        }
    }

    // Update existing Live Activity
    @objc public func updateLiveActivity(
        cycleNumber: Int,
        totalCycles: Int,
        segmentType: String,
        remainingSeconds: Int,
        totalSeconds: Int,
        isPaused: Bool
    ) {
        // If no active activity, try to restart it using stored session info
        guard let activity = currentActivity else {
            print("⚠️ PomodoroLiveActivityManager: No active Live Activity, attempting to restart...")

            // Try to restart if we have session info
            if let sessionId = currentSessionId, let quote = currentQuote {
                print("   Restarting with session: \(sessionId)")
                startLiveActivity(
                    sessionId: sessionId,
                    quote: quote,
                    cycleNumber: cycleNumber,
                    totalCycles: totalCycles,
                    segmentType: segmentType,
                    remainingSeconds: remainingSeconds,
                    totalSeconds: totalSeconds,
                    isPaused: isPaused
                )
            } else {
                print("   ❌ Cannot restart: no session info stored")
            }
            return
        }

        let targetEndTime = isPaused ? Date() : Date().addingTimeInterval(TimeInterval(remainingSeconds))

        let updatedState = PomodoroActivityAttributes.ContentState(
            cycleNumber: cycleNumber,
            totalCycles: totalCycles,
            segmentType: segmentType,
            remainingSeconds: remainingSeconds,
            totalSeconds: totalSeconds,
            isPaused: isPaused,
            targetEndTime: targetEndTime
        )

        Task {
            await activity.update(.init(state: updatedState, staleDate: nil))
            print("PomodoroLiveActivityManager: Live Activity updated")
        }
    }

    // End the Live Activity
    @objc public func endLiveActivity() {
        guard let activity = currentActivity else {
            return
        }

        Task {
            await activity.end(nil, dismissalPolicy: .immediate)
            currentActivity = nil
            currentSessionId = nil
            currentQuote = nil
            print("PomodoroLiveActivityManager: Live Activity ended")
        }
    }

    // End with final state (e.g., completion message)
    @objc public func endLiveActivityWithCompletion(
        completedCycles: Int,
        totalFocusMinutes: Int,
        totalBreakMinutes: Int
    ) {
        guard let activity = currentActivity else {
            return
        }

        let finalState = PomodoroActivityAttributes.ContentState(
            cycleNumber: completedCycles,
            totalCycles: completedCycles,
            segmentType: "completed",
            remainingSeconds: 0,
            totalSeconds: 1,
            isPaused: false,
            targetEndTime: Date()
        )

        Task {
            await activity.end(
                .init(state: finalState, staleDate: nil),
                dismissalPolicy: .default
            )
            currentActivity = nil
            currentSessionId = nil
            currentQuote = nil
            print("PomodoroLiveActivityManager: Live Activity ended with completion")
        }
    }

    // Check if Live Activities are supported
    @objc public static func isSupported() -> Bool {
        if #available(iOS 16.2, *) {
            return ActivityAuthorizationInfo().areActivitiesEnabled
        }
        return false
    }
}
