import ActivityKit
import WidgetKit
import SwiftUI

// MARK: - Activity Attributes
@available(iOS 16.2, *)
struct PomodoroActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        var cycleNumber: Int
        var totalCycles: Int
        var segmentType: String
        var remainingSeconds: Int
        var totalSeconds: Int
        var isPaused: Bool
        var targetEndTime: Date
    }

    var sessionId: String
    var quote: String
}

// MARK: - Live Activity Widget
@available(iOS 16.2, *)
struct PomodoroLiveActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: PomodoroActivityAttributes.self) { context in
            // Lock screen / notification area UI
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Image(systemName: segmentIcon(for: context.state.segmentType))
                        .foregroundColor(segmentColor(for: context.state.segmentType))

                    Text(segmentLabel(for: context.state.segmentType))
                        .font(.headline)

                    Spacer()

                    Text("Cycle \(context.state.cycleNumber)/\(context.state.totalCycles)")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }

                HStack {
                    Spacer()

                    VStack(spacing: 4) {
                        if context.state.isPaused {
                            Text(formatTime(seconds: context.state.remainingSeconds))
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
                            Text(context.state.targetEndTime, style: .timer)
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
                    value: Double(context.state.totalSeconds - context.state.remainingSeconds),
                    total: Double(context.state.totalSeconds)
                )
                    .tint(segmentColor(for: context.state.segmentType))
                    .scaleEffect(y: 2)

                if !context.attributes.quote.isEmpty {
                    Text(context.attributes.quote)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
            }
            .padding()
        } dynamicIsland: { context in
            // Dynamic Island support for iPhone 14 Pro+
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Text(segmentLabel(for: context.state.segmentType))
                        .font(.headline)
                        .fontWeight(.semibold)
                }

                DynamicIslandExpandedRegion(.center) {
                    Text("Cycle \(context.state.cycleNumber)/\(context.state.totalCycles)")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }

                DynamicIslandExpandedRegion(.trailing) {
                    HStack(spacing: 6) {
                        if context.state.isPaused {
                            Image(systemName: "pause.circle.fill")
                                .foregroundColor(.orange)
                        }

                        Text(formatTime(seconds: context.state.remainingSeconds))
                            .font(.title2)
                            .monospacedDigit()
                    }
                }

                DynamicIslandExpandedRegion(.bottom) {
                    ProgressView(
                        value: Double(context.state.totalSeconds - context.state.remainingSeconds),
                        total: Double(context.state.totalSeconds)
                    )
                        .tint(segmentColor(for: context.state.segmentType))
                }
            } compactLeading: {
                Image(systemName: segmentIcon(for: context.state.segmentType))
                    .foregroundColor(segmentColor(for: context.state.segmentType))
            } compactTrailing: {
                HStack(spacing: 2) {
                    if context.state.isPaused {
                        Image(systemName: "pause.circle.fill")
                    }

                    Text(formatTime(seconds: context.state.remainingSeconds))
                        .font(.caption2)
                        .monospacedDigit()
                }
            } minimal: {
                Image(systemName: "timer")
                    .foregroundColor(segmentColor(for: context.state.segmentType))
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
