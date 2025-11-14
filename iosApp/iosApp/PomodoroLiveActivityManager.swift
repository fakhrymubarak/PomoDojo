import ActivityKit
import Foundation

@available(iOS 16.2, *)
@objc public class PomodoroLiveActivityManager: NSObject {
    private var currentActivity: Activity<PomodoroActivityAttributes>?
    private var currentSessionId: String?
    private var currentQuote: String?
    private let storage = UserDefaults.standard
    private let sessionKey = "PomodoroLiveActivity.sessionId"
    private let quoteKey = "PomodoroLiveActivity.quote"
    private let activityIdKey = "PomodoroLiveActivity.activityId"
    private let decoder = JSONDecoder()

    @objc public static let shared = PomodoroLiveActivityManager()

    private struct SegmentSchedulePayload: Codable {
        let generatedAtEpochMillis: Double
        let segments: [PomodoroActivityAttributes.SegmentScheduleItem]
    }

    private override init() {
        super.init()
        currentSessionId = storage.string(forKey: sessionKey)
        currentQuote = storage.string(forKey: quoteKey)
        DispatchQueue.main.async { [weak self] in
            self?.restoreExistingActivityIfNeeded()
        }
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
        isPaused: Bool,
        scheduleJSON: String?
    ) {
        DispatchQueue.main.async { [weak self] in
            self?.performStartLiveActivity(
                sessionId: sessionId,
                quote: quote,
                cycleNumber: cycleNumber,
                totalCycles: totalCycles,
                segmentType: segmentType,
                remainingSeconds: remainingSeconds,
                totalSeconds: totalSeconds,
                isPaused: isPaused,
                scheduleJSON: scheduleJSON
            )
        }
    }

    private func performStartLiveActivity(
        sessionId: String,
        quote: String,
        cycleNumber: Int,
        totalCycles: Int,
        segmentType: String,
        remainingSeconds: Int,
        totalSeconds: Int,
        isPaused: Bool,
        scheduleJSON: String?
    ) {
        print("🚀 PomodoroLiveActivityManager: startLiveActivity called")
        print("   Session: \(sessionId)")
        print("   Quote: \(quote.prefix(30))...")
        print("   Cycle: \(cycleNumber)/\(totalCycles)")
        print("   Segment: \(segmentType)")
        print("   Remaining: \(remainingSeconds)s")
        print("   Paused: \(isPaused)")

        // End any existing activity first
        performEndLiveActivity(clearStoredData: true)

        // Store session info for potential restarts
        currentSessionId = sessionId
        currentQuote = quote

        let attributes = PomodoroActivityAttributes(
            sessionId: sessionId,
            quote: quote
        )

        let targetEndTime = isPaused ? Date() : Date().addingTimeInterval(TimeInterval(remainingSeconds))

        let scheduleState = normalizedSchedule(
            scheduleJSON: scheduleJSON,
            fallbackType: segmentType,
            cycleNumber: cycleNumber,
            totalSeconds: totalSeconds,
            remainingSeconds: remainingSeconds
        )

        let contentState = PomodoroActivityAttributes.ContentState(
            cycleNumber: cycleNumber,
            totalCycles: totalCycles,
            segmentType: segmentType,
            remainingSeconds: remainingSeconds,
            totalSeconds: totalSeconds,
            isPaused: isPaused,
            targetEndTime: targetEndTime,
            scheduleGeneratedAt: scheduleState.generatedAt,
            segmentSchedule: scheduleState.segments
        )

        do {
            let activity = try Activity.request(
                attributes: attributes,
                content: .init(state: contentState, staleDate: nil),
                pushType: nil
            )
            adoptActivity(activity, sessionId: sessionId, quote: quote)
            print("✅ PomodoroLiveActivityManager: Live Activity started successfully!")
            print("   Activity ID: \(activity.id)")
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
        isPaused: Bool,
        scheduleJSON: String?
    ) {
        DispatchQueue.main.async { [weak self] in
            self?.performUpdateLiveActivity(
                cycleNumber: cycleNumber,
                totalCycles: totalCycles,
                segmentType: segmentType,
                remainingSeconds: remainingSeconds,
                totalSeconds: totalSeconds,
                isPaused: isPaused,
                scheduleJSON: scheduleJSON
            )
        }
    }

    private func performUpdateLiveActivity(
        cycleNumber: Int,
        totalCycles: Int,
        segmentType: String,
        remainingSeconds: Int,
        totalSeconds: Int,
        isPaused: Bool,
        scheduleJSON: String?
    ) {
        // If no active activity, try to restart it using stored session info
        guard let activity = resolveActiveActivity() else {
            print("⚠️ PomodoroLiveActivityManager: No active Live Activity, attempting to restart...")

            // Try to restart if we have session info
            if let sessionId = currentSessionId, let quote = currentQuote {
                print("   Restarting with session: \(sessionId)")
                performStartLiveActivity(
                    sessionId: sessionId,
                    quote: quote,
                    cycleNumber: cycleNumber,
                    totalCycles: totalCycles,
                    segmentType: segmentType,
                    remainingSeconds: remainingSeconds,
                    totalSeconds: totalSeconds,
                    isPaused: isPaused,
                    scheduleJSON: scheduleJSON
                )
            } else {
                print("   ❌ Cannot restart: no session info stored")
            }
            return
        }

        let targetEndTime = isPaused ? Date() : Date().addingTimeInterval(TimeInterval(remainingSeconds))

        let scheduleState = normalizedSchedule(
            scheduleJSON: scheduleJSON,
            fallbackType: segmentType,
            cycleNumber: cycleNumber,
            totalSeconds: totalSeconds,
            remainingSeconds: remainingSeconds
        )

        let updatedState = PomodoroActivityAttributes.ContentState(
            cycleNumber: cycleNumber,
            totalCycles: totalCycles,
            segmentType: segmentType,
            remainingSeconds: remainingSeconds,
            totalSeconds: totalSeconds,
            isPaused: isPaused,
            targetEndTime: targetEndTime,
            scheduleGeneratedAt: scheduleState.generatedAt,
            segmentSchedule: scheduleState.segments
        )

        Task {
            await activity.update(.init(state: updatedState, staleDate: nil))
            adoptActivity(activity)
            print("PomodoroLiveActivityManager: Live Activity updated")
        }
    }

    // End the Live Activity
    @objc public func endLiveActivity() {
        DispatchQueue.main.async { [weak self] in
            self?.performEndLiveActivity(clearStoredData: true)
        }
    }

    private func performEndLiveActivity(clearStoredData: Bool) {
        guard let activity = resolveActiveActivity() else {
            if clearStoredData {
                clearStoredSession()
            }
            return
        }

        Task {
            await activity.end(nil, dismissalPolicy: .immediate)
            currentActivity = nil
            currentSessionId = nil
            currentQuote = nil
            if clearStoredData {
                clearStoredSession()
            }
            print("PomodoroLiveActivityManager: Live Activity ended")
        }
    }

    // End with final state (e.g., completion message)
    @objc public func endLiveActivityWithCompletion(
        completedCycles: Int,
        totalFocusMinutes: Int,
        totalBreakMinutes: Int
    ) {
        DispatchQueue.main.async { [weak self] in
            self?.performEndLiveActivityWithCompletion(
                completedCycles: completedCycles,
                totalFocusMinutes: totalFocusMinutes,
                totalBreakMinutes: totalBreakMinutes
            )
        }
    }

    private func performEndLiveActivityWithCompletion(
        completedCycles: Int,
        totalFocusMinutes: Int,
        totalBreakMinutes: Int
    ) {
        guard let activity = resolveActiveActivity() else {
            clearStoredSession()
            return
        }

        let finalState = PomodoroActivityAttributes.ContentState(
            cycleNumber: completedCycles,
            totalCycles: completedCycles,
            segmentType: "completed",
            remainingSeconds: 0,
            totalSeconds: 1,
            isPaused: false,
            targetEndTime: Date(),
            scheduleGeneratedAt: Date(),
            segmentSchedule: []
        )

        Task {
            await activity.end(
                .init(state: finalState, staleDate: nil),
                dismissalPolicy: .default
            )
            currentActivity = nil
            currentSessionId = nil
            currentQuote = nil
            clearStoredSession()
            print("PomodoroLiveActivityManager: Live Activity ended with completion")
        }
    }

    // Check if Live Activities are supported
    @objc public static func isSupported() -> Bool {
        ActivityAuthorizationInfo().areActivitiesEnabled
    }

    private var storedActivityId: String? {
        storage.string(forKey: activityIdKey)
    }

    private func adoptActivity(
        _ activity: Activity<PomodoroActivityAttributes>,
        sessionId: String? = nil,
        quote: String? = nil
    ) {
        currentActivity = activity
        let resolvedSessionId = sessionId ?? activity.attributes.sessionId
        let resolvedQuote = quote ?? activity.attributes.quote
        currentSessionId = resolvedSessionId
        currentQuote = resolvedQuote
        persistSession(sessionId: resolvedSessionId, quote: resolvedQuote, activityId: activity.id)
    }

    private func resolveActiveActivity() -> Activity<PomodoroActivityAttributes>? {
        if let activity = currentActivity {
            return activity
        }

        if let targetId = storedActivityId,
           let storedActivity = Activity<PomodoroActivityAttributes>.activities.first(where: { $0.id == targetId }) {
            adoptActivity(storedActivity)
            return storedActivity
        }

        if let firstActivity = Activity<PomodoroActivityAttributes>.activities.first {
            adoptActivity(firstActivity)
            return firstActivity
        }

        return nil
    }

    private func restoreExistingActivityIfNeeded() {
        _ = resolveActiveActivity()
    }

    private func persistSession(sessionId: String?, quote: String?, activityId: String?) {
        if let sessionId {
            storage.set(sessionId, forKey: sessionKey)
        } else {
            storage.removeObject(forKey: sessionKey)
        }

        if let quote {
            storage.set(quote, forKey: quoteKey)
        } else {
            storage.removeObject(forKey: quoteKey)
        }

        if let activityId {
            storage.set(activityId, forKey: activityIdKey)
        } else {
            storage.removeObject(forKey: activityIdKey)
        }
    }

    private func clearStoredSession() {
        persistSession(sessionId: nil, quote: nil, activityId: nil)
    }

    private func normalizedSchedule(
        scheduleJSON: String?,
        fallbackType: String,
        cycleNumber: Int,
        totalSeconds: Int,
        remainingSeconds: Int
    ) -> (generatedAt: Date, segments: [PomodoroActivityAttributes.SegmentScheduleItem]) {
        if let decoded = decodeSchedule(scheduleJSON: scheduleJSON) {
            return decoded
        }
        let generatedAt = Date()
        let safeRemaining = max(remainingSeconds, 0)
        let elapsed = max(0, totalSeconds - safeRemaining)
        let item = PomodoroActivityAttributes.SegmentScheduleItem(
            type: fallbackType,
            cycleNumber: cycleNumber,
            totalSeconds: totalSeconds,
            startOffsetSeconds: -elapsed
        )
        return (generatedAt, [item])
    }

    private func decodeSchedule(
        scheduleJSON: String?
    ) -> (generatedAt: Date, segments: [PomodoroActivityAttributes.SegmentScheduleItem])? {
        guard
            let scheduleJSON,
            !scheduleJSON.isEmpty,
            let data = scheduleJSON.data(using: .utf8),
            let payload = try? decoder.decode(SegmentSchedulePayload.self, from: data)
        else {
            return nil
        }

        let generatedAt = Date(timeIntervalSince1970: payload.generatedAtEpochMillis / 1000.0)
        return (generatedAt, payload.segments)
    }
}
