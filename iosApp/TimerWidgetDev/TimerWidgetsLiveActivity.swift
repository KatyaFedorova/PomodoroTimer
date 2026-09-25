import ActivityKit
import WidgetKit
import SwiftUI


struct TimerWidgetsLiveActivity: Widget {

    private func formatTimeInterval(_ interval: TimeInterval?) -> String {
        guard let interval = interval, interval > 0 else { return "00:00" }
        let formatter = DateComponentsFormatter()
        formatter.allowedUnits = [.minute, .second]
        formatter.unitsStyle = .positional
        formatter.zeroFormattingBehavior = .pad
        return formatter.string(from: interval) ?? "00:00"
    }

    var body: some WidgetConfiguration {
        ActivityConfiguration(for: TimerWidgetsAttributes.self) { context in
            VStack {
                HStack(spacing: 15) {
                    let size: CGFloat = 80
                    ZStack {
                        Image("clock")
                            .resizable()
                            .scaledToFit()
                            .padding(5)
                    }
                    .frame(width: size, height: size)
                    .background(Color.white)
                    .clipShape(Circle())
                    VStack(alignment: .leading, spacing: 8) {
                        if context.state.isFinished {
                            let displayText = context.attributes.isBreak ? "Meow! Time for another Kittidoro session!": "Break time! Pick a move and go!";
                            Text(displayText)
                                .font(.headline)
                                .foregroundStyle(.white)
                                .bold()
                        } else {
                            Text(context.state.displayText)
                                .font(.body)
                                .foregroundStyle(.white)
                                .bold()

                            Text(timerInterval: context.attributes.startDate...context.attributes.endDate, countsDown: true, showsHours: false)
                                .font(.title)
                                .foregroundStyle(.white)
                                .bold()
                                .monospacedDigit()

                            ProgressView(timerInterval: context.attributes.startDate...context.attributes.endDate, countsDown: false)
                                .progressViewStyle(LinearProgressViewStyle())
                                .tint(.white)
                                .frame(height: 10, alignment: .top)
                                .clipped()

                        }
                    }
                    Spacer()
                }
                .padding(.vertical, 15)
                .padding(.horizontal, 20)
            }
            .activityBackgroundTint(Color.black.opacity(0.7))
            .activitySystemActionForegroundColor(Color.white)

        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Image(systemName: "timer")
                        .font(.title2)
                        .foregroundColor(.cyan)
                }
                DynamicIslandExpandedRegion(.trailing) {
                    if context.state.isFinished {
                        Text("🎉")
                            .font(.title2.bold())
                            .foregroundColor(.green)
                    } else {
                        Text(timerInterval: context.attributes.startDate...context.attributes.endDate, countsDown: true, showsHours: false)
                            .font(.title2.bold().monospacedDigit())
                            .foregroundColor(.cyan)
                            .frame(width: 80)
                    }
                }
                DynamicIslandExpandedRegion(.center) {
                    Text(context.state.displayText)
                        .font(.headline)
                        .lineLimit(1)
                        .foregroundColor(context.state.isFinished ? .green : .primary)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    if !context.state.isFinished {
                        ProgressView(timerInterval: context.attributes.startDate...context.attributes.endDate, countsDown: false)
                            .progressViewStyle(LinearProgressViewStyle())
                            .tint(.cyan)
                            .frame(height: 8, alignment: .top)
                            .clipped()
                    }
                }
            } compactLeading: {
                Image(systemName: context.state.isFinished ? "checkmark.circle.fill" : "timer")
                    .foregroundColor(context.state.isFinished ? .green : .cyan)
                    .padding([.leading], 2)
            } compactTrailing: {
                if context.state.isFinished {
                    Text("Done")
                        .font(.caption)
                        .foregroundColor(.green)
                } else {
                    Text(timerInterval: context.attributes.startDate...context.attributes.endDate, countsDown: true, showsHours: false)
                        .font(.caption.monospacedDigit())
                        .foregroundColor(.cyan)
                        .frame(width: 45)
                        .padding([.trailing], 2)
                }
            } minimal: {
                Image(systemName: context.state.isFinished ? "checkmark.circle.fill" : "timer.square")
                    .foregroundColor(context.state.isFinished ? .green : .cyan)
            }
            .widgetURL(URL(string: "kittidoro://openTimer"))
            .keylineTint(Color.cyan)
        }
    }
}
