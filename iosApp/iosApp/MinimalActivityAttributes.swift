import ActivityKit

  struct MinimalActivityAttributes: ActivityAttributes {
      public struct ContentState: Codable, Hashable {
          var displayText: String
      }
      var activityName: String
  }
