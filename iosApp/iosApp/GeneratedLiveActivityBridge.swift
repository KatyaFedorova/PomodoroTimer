// Claude Code is working ✅
import Foundation
import ActivityKit
import Combine
import SwiftUI

@available(iOS 16.2, *)
class GeneratedLiveActivityBridge: ObservableObject {
    
    static let shared = GeneratedLiveActivityBridge()
    
    @Published private(set) var currentActivity: Activity<TimerWidgetsAttributes>?
    @Published private(set) var currentActivityPushToken: String?
    
    private var activityObservationTask: Task<Void, Never>? = nil
    private var pushTokenObservationTask: Task<Void, Never>? = nil
    private let liveActivityScheduler = LiveActivityScheduler()
    private var currentActivityTargetEndDate: Date?
    
    deinit {
        activityObservationTask?.cancel()
        pushTokenObservationTask?.cancel()
    }
    
    func startActivity(categoryName: String, isBreak: Bool, totalDurationMillis: Int64, soundFileName: String) {
        guard ActivityAuthorizationInfo().areActivitiesEnabled else {
            print("Live Activities are not enabled.")
            return
        }
        
        endCurrentLiveActivity(dismissalPolicy: .immediate, clearInternalStateForNewActivity: true)
        print("Previous activity processing ended, preparing to start new one.")
        
        let now = Date()
        let totalDurationSeconds = TimeInterval(totalDurationMillis) / 1000.0
        
        guard totalDurationSeconds > 0 else {
            print("Total duration is not positive, cannot start activity.")
            return
        }
        
        let activitySpecificTargetEndDate = now.addingTimeInterval(totalDurationSeconds)
        
        let attributes = TimerWidgetsAttributes(
            startDate: now,
            endDate: activitySpecificTargetEndDate,
            isBreak: isBreak
        )
        
        let initialContentState = TimerWidgetsAttributes.ContentState(
            displayText: categoryName,
            isFinished: false
        )
        
        let staleDate = Calendar.current.date(byAdding: .hour, value: 4, to: activitySpecificTargetEndDate) ?? activitySpecificTargetEndDate.addingTimeInterval(4 * 60 * 60)
        let relevanceScore = 100.0
        
        let activityContent = ActivityContent(
            state: initialContentState,
            staleDate: staleDate,
            relevanceScore: relevanceScore
        )
        
        Task {
            do {
                let newActivity = try Activity<TimerWidgetsAttributes>.request(
                    attributes: attributes,
                    content: activityContent,
                    pushType: .token // .token enables the APNs push token used to schedule the end-of-timer update; nil = local only
                )
                print("Live Activity requested with ID: \(newActivity.id), Target End Date for this activity: \(activitySpecificTargetEndDate)")
                
                await MainActor.run {
                    self.currentActivity = newActivity
                    self.currentActivityPushToken = nil
                    self.currentActivityTargetEndDate = activitySpecificTargetEndDate
                }
                
                observeActivityState(activity: newActivity)
                observePushToken(for: newActivity,
                                 associatedTargetEndDate: activitySpecificTargetEndDate,
                                 soundFileName: soundFileName)
                
                print("ACTION REQUIRED (startActivity): Send targetEndDate (\(activitySpecificTargetEndDate)) and activity.id (\(newActivity.id)) to your server.")
                
            } catch {
                print("Error requesting Live Activity: \(error.localizedDescription)")
                await MainActor.run {
                    self.currentActivity = nil
                    self.currentActivityPushToken = nil
                    self.currentActivityTargetEndDate = nil
                }
            }
        }
    }
    
    private func observePushToken(for activity: Activity<TimerWidgetsAttributes>, associatedTargetEndDate: Date, soundFileName:String) {
        if self.currentActivity?.id == activity.id {
            self.pushTokenObservationTask?.cancel()
            self.pushTokenObservationTask = Task {
                await processPushTokenStream(for: activity, associatedTargetEndDate: associatedTargetEndDate,soundFileName: soundFileName)
            }
        } else {
            Task {
                print("Observing push token for activity \(activity.id) which is not currently self.currentActivity (\(String(describing: self.currentActivity?.id))). This task is independent of the global pushTokenObservationTask.")
                await processPushTokenStream(for: activity, associatedTargetEndDate: associatedTargetEndDate,soundFileName:soundFileName)
            }
        }
    }
    
    private func processPushTokenStream(for activity: Activity<TimerWidgetsAttributes>, associatedTargetEndDate: Date, soundFileName: String) async {
        print("processPushTokenStream: Starting to observe push token for activity \(activity.id)")
        
        for try await pushTokenData in activity.pushTokenUpdates {
            let pushTokenString = pushTokenData.map { String(format: "%02x", $0) }.joined()
            
            await MainActor.run {
                if self.currentActivity?.id == activity.id {
                    self.currentActivityPushToken = pushTokenString
                }
            }
            print("--------------------------------------------------------------------")
            print("✅ LIVE ACTIVITY APNs PUSH TOKEN: \(pushTokenString) for activity ID \(activity.id)")
            print("--------------------------------------------------------------------")
            
            guard self.currentActivity?.id == activity.id else {
                print("⚠️ Activity mismatch during push token processing. Current activity is \(String(describing: self.currentActivity?.id)), but token is for \(activity.id). Skipping server schedule for this token.")
                continue
            }
            
            print("📲 Calling LiveActivityScheduler to schedule server update for token: \(pushTokenString) at \(associatedTargetEndDate) (for activity \(activity.id))")
            self.liveActivityScheduler.scheduleActivityUpdate(
                liveActivityPushToken: pushTokenString,
                endTime: associatedTargetEndDate,
                soundFileName: soundFileName
            )
        }
        print("processPushTokenStream: Finished observing push token for activity \(activity.id)")
    }
    
    func endCurrentLiveActivity(dismissalPolicy: ActivityUIDismissalPolicy = .default, clearInternalStateForNewActivity: Bool = false) {
        if clearInternalStateForNewActivity {
            print("endCurrentLiveActivity: Clearing internal state for new activity.")
            activityObservationTask?.cancel()
            pushTokenObservationTask?.cancel()
            activityObservationTask = nil
            pushTokenObservationTask = nil
            if Thread.isMainThread { self.currentActivityTargetEndDate = nil }
            else { DispatchQueue.main.async { self.currentActivityTargetEndDate = nil } }
        }
        
        guard let activityToEnd = self.currentActivity else {
            print("endCurrentLiveActivity: No 'self.currentActivity' to end. Checking system activities.")
            if Activity<TimerWidgetsAttributes>.activities.isEmpty == false {
                Task {
                    print("endCurrentLiveActivity: Ending all system activities of type TimerWidgetsAttributes.")
                    for activityInSystem in Activity<TimerWidgetsAttributes>.activities {
                        await activityInSystem.end(nil, dismissalPolicy: dismissalPolicy)
                    }
                }
            }
            if clearInternalStateForNewActivity {
                if Thread.isMainThread {
                    self.currentActivity = nil
                    self.currentActivityPushToken = nil
                } else { DispatchQueue.main.async {
                    self.currentActivity = nil
                    self.currentActivityPushToken = nil
                }}
            }
            return
        }
        
        print("endCurrentLiveActivity: Ending activity with ID \(activityToEnd.id).")
        Task {
            await activityToEnd.end(nil, dismissalPolicy: dismissalPolicy)
        }
    }
    
    func endActivityByUserCancel() {
        print("endActivityByUserCancel called.")
        endCurrentLiveActivity(dismissalPolicy: .immediate)
    }
    
    private func clearTrackedActivityReferences() {
        print("clearTrackedActivityReferences: Clearing all tracked references for current activity.")
        if Thread.isMainThread {
            self.currentActivity = nil
            self.currentActivityPushToken = nil
            self.currentActivityTargetEndDate = nil
        } else {
            DispatchQueue.main.async {
                self.currentActivity = nil
                self.currentActivityPushToken = nil
                self.currentActivityTargetEndDate = nil
            }
        }
        // Cancel observation tasks that were associated with the now-cleared currentActivity
        self.activityObservationTask?.cancel()
        self.pushTokenObservationTask?.cancel() // Also cancel the push token task for the cleared activity
        self.activityObservationTask = nil
        self.pushTokenObservationTask = nil
    }
    
    private func observeActivityState(activity: Activity<TimerWidgetsAttributes>) {
        if self.currentActivity?.id == activity.id {
            self.activityObservationTask?.cancel() // Cancel previous global task
            self.activityObservationTask = Task { // Assign new global task
                await processActivityStateStream(for: activity)
            }
        } else {
            Task { // Local task, doesn't overwrite self.activityObservationTask
                print("Observing activity state for activity \(activity.id) which is not currently self.currentActivity (\(String(describing: self.currentActivity?.id))). This task is independent of the global activityObservationTask.")
                await processActivityStateStream(for: activity)
            }
        }
    }
    
    private func processActivityStateStream(for activity: Activity<TimerWidgetsAttributes>) async {
        print("processActivityStateStream: Starting to observe activity state for \(activity.id)")
        for await currentActivityState in activity.activityStateUpdates {
            print("Activity \(activity.id) state update: \(currentActivityState)")
            
            if currentActivityState == .dismissed || currentActivityState == .ended {
                print("Activity \(activity.id) is \(currentActivityState).")
                
                if self.currentActivity?.id == activity.id {
                    print("Clearing tracked references because the current activity (\(activity.id)) has \(currentActivityState).")
                    clearTrackedActivityReferences()
                } else {
                    print("Activity \(activity.id) is \(currentActivityState), but it was not the 'self.currentActivity' (\(String(describing: self.currentActivity?.id))). No global references cleared by this event.")
                }
                break // Stop observing this activity's state as it's terminally ended/dismissed
            }
        }
        if Task.isCancelled {
            print("processActivityStateStream: Activity state observation for \(activity.id) was cancelled.")
        }
        print("processActivityStateStream: Finished observing activity state for \(activity.id)")
    }
}



