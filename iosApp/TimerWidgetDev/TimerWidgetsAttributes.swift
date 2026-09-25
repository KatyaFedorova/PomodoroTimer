import ActivityKit
import Foundation

public struct TimerWidgetsAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        var displayText: String
        var isFinished: Bool
    }
    
    var startDate: Date
    var endDate: Date
    var isBreak:Bool
}
