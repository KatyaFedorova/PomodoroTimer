import SwiftUI
import FirebaseMessaging
import FirebaseAuth
import ComposeApp

@main
struct iOSApp: App {
    
    let liveActivityBridge = GeneratedLiveActivityBridge.shared
    
    init() {
        MobileAlarm_iosKt.startLiveActivity = { title, isBreak, timeLeftInMillis, soundFileName in
            GeneratedLiveActivityBridge.shared.startActivity(
                categoryName: title,
                isBreak: isBreak.boolValue,
                totalDurationMillis: timeLeftInMillis.int64Value,
                soundFileName: soundFileName
            )
        }
        
        MobileAlarm_iosKt.cancelLiveActivity = {
            GeneratedLiveActivityBridge.shared.endActivityByUserCancel()
        }
        
        DefaultAuthGateway_iosKt.signInWithGoogle = {  callback in
            FirebaseAuthBridge.shared.signInWithGoogle { isSucsess,error in
                _ = callback(KotlinBoolean(bool: isSucsess), error)
            }
        }
        
        DefaultAuthGateway_iosKt.signOut = {
            FirebaseAuthBridge.shared.signOut()
        }
        
        DefaultAuthGateway_iosKt.observeAuthState = { callback in
            FirebaseAuthBridge.shared.observeAuthState { isLoggedIn,id,email,name  in
                let kotlinBool = KotlinBoolean(bool: isLoggedIn)
                _ = callback(kotlinBool,id,email,name)
            }
        }
        
    }
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    print("✅ onOpenURL (SwiftUI):", url.absoluteString)
                    let vm = IOSViewModelProvider().authViewModel
                    vm.onMagickLinkClick(url: url.absoluteString)
                }
        }
    }
}
