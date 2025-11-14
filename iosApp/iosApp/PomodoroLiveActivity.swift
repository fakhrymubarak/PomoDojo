import ActivityKit
import WidgetKit
import SwiftUI

// MARK: - Activity Attributes
@available(iOS 16.2, *)
struct PomodoroActivityAttributes: ActivityAttributes {
    public struct SegmentScheduleItem: Codable, Hashable {
        var type: String
        var cycleNumber: Int
        var totalSeconds: Int
        var startOffsetSeconds: Int
    }

    public struct ContentState: Codable, Hashable {
        var cycleNumber: Int
        var totalCycles: Int
        var segmentType: String
        var remainingSeconds: Int
        var totalSeconds: Int
        var isPaused: Bool
        var targetEndTime: Date
        var scheduleGeneratedAt: Date
        var segmentSchedule: [SegmentScheduleItem]
    }

    var sessionId: String
    var quote: String
}

// MARK: - Live Activity Widget
@available(iOS 16.2, *)
struct PomodoroLiveActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: PomodoroActivityAttributes.self) { context in
            ActiveSegmentTimelineView(state: context.state) { activeSegment in
                // Lock screen / notification area UI
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Image(systemName: segmentIcon(for: activeSegment.type))
                            .foregroundColor(segmentColor(for: activeSegment.type))

                        Text(segmentLabel(for: activeSegment.type))
                            .font(.headline)

                        Spacer()

                        Text("Cycle \(activeSegment.cycleNumber)/\(context.state.totalCycles)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }

                    HStack {
                        Spacer()

                        VStack(spacing: 4) {
                            if activeSegment.isPaused {
                                Text(formatTime(seconds: activeSegment.remainingSeconds))
                                    .font(.system(size: 32, weight: .bold, design: .rounded))
                                    .monospacedDigit()
                                    .frame(maxWidth: .infinity, alignment: .center)
                                    .multilineTextAlignment(.center)
                                Text("Paused")
                                    .font(.caption)
                                    .foregroundColor(.orange)
                                    .frame(maxWidth: .infinity, alignment: .center)
                                    .multilineTextAlignment(.center)
                            } else {
                                Text(activeSegment.targetEndTime, style: .timer)
                                    .font(.system(size: 32, weight: .bold, design: .rounded))
                                    .monospacedDigit()
                                    .frame(maxWidth: .infinity, alignment: .center)
                                    .multilineTextAlignment(.center)
                            }
                        }
                        .frame(maxWidth: .infinity)

                        Spacer()
                    }

                    ProgressView(
                        value: Double(activeSegment.totalSeconds - activeSegment.remainingSeconds),
                        total: Double(activeSegment.totalSeconds)
                    )
                        .tint(segmentColor(for: activeSegment.type))
                        .scaleEffect(y: 2)

                    if !context.attributes.quote.isEmpty {
                        Text(context.attributes.quote)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                }
            }
            .padding()
        } dynamicIsland: { context in
            // Dynamic Island support for iPhone 14 Pro+
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    ActiveSegmentTimelineView(state: context.state) { activeSegment in
                        Text(segmentLabel(for: activeSegment.type))
                            .font(.headline)
                            .fontWeight(.semibold)
                    }
                }

                DynamicIslandExpandedRegion(.center) {
                    ActiveSegmentTimelineView(state: context.state) { activeSegment in
                        Text("Cycle \(activeSegment.cycleNumber)/\(context.state.totalCycles)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }

                DynamicIslandExpandedRegion(.trailing) {
                    ActiveSegmentTimelineView(state: context.state) { activeSegment in
                        HStack(spacing: 6) {
                            if activeSegment.isPaused {
                                Image(systemName: "pause.circle.fill")
                                    .foregroundColor(.orange)
                            }

                            Text(formatTime(seconds: activeSegment.remainingSeconds))
                                .font(.title2)
                                .monospacedDigit()
                        }
                    }
                }

                DynamicIslandExpandedRegion(.bottom) {
                    ActiveSegmentTimelineView(state: context.state) { activeSegment in
                        ProgressView(
                            value: Double(activeSegment.totalSeconds - activeSegment.remainingSeconds),
                            total: Double(activeSegment.totalSeconds)
                        )
                            .tint(segmentColor(for: activeSegment.type))
                    }
                }
            } compactLeading: {
                ActiveSegmentTimelineView(state: context.state) { activeSegment in
                    Image(systemName: segmentIcon(for: activeSegment.type))
                        .foregroundColor(segmentColor(for: activeSegment.type))
                }
            } compactTrailing: {
                ActiveSegmentTimelineView(state: context.state) { activeSegment in
                    HStack(spacing: 2) {
                        if activeSegment.isPaused {
                            Image(systemName: "pause.circle.fill")
                        }

                        Text(formatTime(seconds: activeSegment.remainingSeconds))
                            .font(.caption2)
                            .monospacedDigit()
                    }
                }
            } minimal: {
                ActiveSegmentTimelineView(state: context.state) { activeSegment in
                    Image(systemName: "timer")
                        .foregroundColor(segmentColor(for: activeSegment.type))
                }
            }
        }
    }

    private func segmentIcon(for type: String) -> String {
        switch type {
        case "focus": return "brain.head.profile"
        case "short_break", "long_break": return "cup.and.saucer.fill"
        default: return "timer"
        }
    }

    private func segmentLabel(for type: String) -> String {
        switch type {
        case "focus": return "Focus"
        case "short_break": return "Break"
        case "long_break": return "Long Break"
        default: return "Timer"
        }
    }

    private func segmentColor(for type: String) -> Color {
        switch type {
        case "focus": return Color(red: 0x56 / 255.0, green: 0x7D / 255.0, blue: 0x41 / 255.0) // Secondary - green
        case "short_break": return Color(red: 0xBF / 255.0, green: 0x4A / 255.0, blue: 0x35 / 255.0) // Primary - terracotta
        case "long_break": return Color(red: 0xFF / 255.0, green: 0xC8 / 255.0, blue: 0x57 / 255.0) // Long break highlight - yellow
        default: return .gray
        }
    }

    private func formatTime(seconds: Int) -> String {
        let minutes = seconds / 60
        let secs = seconds % 60
        return String(format: "%02d:%02d", minutes, secs)
    }
}

@available(iOS 16.2, *)
private struct ActiveSegmentState {
    let type: String
    let cycleNumber: Int
    let remainingSeconds: Int
    let totalSeconds: Int
    let targetEndTime: Date
    let isPaused: Bool
}

@available(iOS 16.2, *)
private struct ActiveSegmentTimelineView<Content: View>: View {
    let state: PomodoroActivityAttributes.ContentState
    private let content: (ActiveSegmentState) -> Content

    init(
        state: PomodoroActivityAttributes.ContentState,
        @ViewBuilder content: @escaping (ActiveSegmentState) -> Content
    ) {
        self.state = state
        self.content = content
    }

    var body: some View {
        TimelineView(.periodic(from: .now, by: 1)) { timelineContext in
            let activeSegment = resolveActiveSegment(
                for: state,
                referenceDate: timelineContext.date
            )
            content(activeSegment)
        }
    }
}

@available(iOS 16.2, *)
private func resolveActiveSegment(
    for state: PomodoroActivityAttributes.ContentState,
    referenceDate: Date
) -> ActiveSegmentState {
    guard !state.isPaused, !state.segmentSchedule.isEmpty else {
        return ActiveSegmentState(
            type: state.segmentType,
            cycleNumber: state.cycleNumber,
            remainingSeconds: state.remainingSeconds,
            totalSeconds: state.totalSeconds,
            targetEndTime: state.targetEndTime,
            isPaused: state.isPaused
        )
    }

    let elapsedSinceGenerated = Int(referenceDate.timeIntervalSince(state.scheduleGeneratedAt))

    for item in state.segmentSchedule {
        let rawElapsed = elapsedSinceGenerated - item.startOffsetSeconds
        let segmentElapsed = max(rawElapsed, 0)
        if segmentElapsed < item.totalSeconds {
            let remaining = max(0, item.totalSeconds - segmentElapsed)
            let segmentStart = state.scheduleGeneratedAt.addingTimeInterval(
                TimeInterval(item.startOffsetSeconds)
            )
            let targetEnd = segmentStart.addingTimeInterval(TimeInterval(item.totalSeconds))
            return ActiveSegmentState(
                type: item.type,
                cycleNumber: item.cycleNumber,
                remainingSeconds: remaining,
                totalSeconds: item.totalSeconds,
                targetEndTime: targetEnd,
                isPaused: state.isPaused
            )
        }
    }

    return ActiveSegmentState(
        type: "completed",
        cycleNumber: state.totalCycles,
        remainingSeconds: 0,
        totalSeconds: 1,
        targetEndTime: referenceDate,
        isPaused: state.isPaused
    )
}

// MARK: - Widget Bundle Registration
// This registers the Live Activity widget with iOS
#if WIDGET_EXTENSION
@main
@available(iOS 16.2, *)
struct PomodoroWidgets: WidgetBundle {
    var body: some Widget {
        PomodoroLiveActivityWidget()
    }
}
#endif
