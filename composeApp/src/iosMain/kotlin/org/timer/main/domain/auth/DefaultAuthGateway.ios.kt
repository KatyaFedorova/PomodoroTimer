package org.timer.main.domain.auth

import com.russhwolf.settings.*
import dev.gitlive.firebase.*
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import platform.Foundation.NSBundle

lateinit var signInWithGoogle: ((Boolean, String?) -> Unit) -> Unit
lateinit var signOut: () -> Unit
lateinit var observeAuthState: ((Boolean, String, String, String?) -> Unit) -> Unit

actual class DefaultAuthGateway actual constructor() : AuthGateway {

    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Loading)
    private val setting = Settings()

    override suspend fun loginWithGoogle() {
        suspendCancellableCoroutine { cont ->
            signInWithGoogle { isSuccess, error ->
                if (isSuccess) {
                    cont.resume(Unit)
                } else {
                    cont.resumeWithException(Exception(error))
                }
            }
        }
    }

    override suspend fun logout() {
        signOut()
    }

    override suspend fun createUser(id: String, email: String, name: String?) {
        val userDocRef = Firebase.firestore
            .collection("users")
            .document(id)

        val snapshot = userDocRef.get()

        val data = mutableMapOf<String, Any>(
            "email" to email
        )
        if (name != null) {
            data["name"] = name
        }

        if (snapshot.exists) {
            userDocRef.set(data, merge = true)
        } else {
            data["createdAt"] = FieldValue.serverTimestamp
            userDocRef.set(data)
        }
    }

    override suspend fun sendMagicLink(email: String) {
        // Pick the environment from the actual app identity (bundle id), NOT the build
        // configuration. Both Dev and Prod targets can be built in Debug (e.g. running from
        // Xcode), so keying off Platform.isDebugBinary would make the Prod app send a dev
        // continue-URL against the prod Firebase project -> error 17038 "Domain not allowlisted".
        val isDevApp = NSBundle.mainBundle.bundleIdentifier == "org.timer.PomodoroTimer.dev"
        val actionCodeSettings = if (isDevApp) {
            ActionCodeSettings(
                url = "https://tomatopawsdev.firebaseapp.com",
                androidPackageName = AndroidPackageName(
                    packageName = "org.timer.dev",
                    installIfNotAvailable = true
                ),
                canHandleCodeInApp = true,
                iOSBundleId = "org.timer.PomodoroTimer.dev",
            )
        } else {
            ActionCodeSettings(
                url = "https://kittidoro-timer.web.app",
                androidPackageName = AndroidPackageName(
                    packageName = "org.timer",
                    installIfNotAvailable = true
                ),
                canHandleCodeInApp = true,
                iOSBundleId = "org.KittydoroTimer",
            )
        }

        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
        setting.putString("email", email)
    }

    override suspend fun completeLoginWithLink(url: String): MagicLinkResult {
        return try {
            val email = setting.getString("email", "")
            Firebase.auth.signInWithEmailLink(email, url)
            MagicLinkResult.Success
        } catch (ex: FirebaseAuthActionCodeException) {
            MagicLinkResult.Error("Link expired or used")
        } catch (e: Throwable) {
            MagicLinkResult.Error(e.message ?: "Unknown error during magic link sign-in")
        }
    }

    override fun getAuthState(): Flow<AuthState> = observedAuthState

    private val observedAuthState: StateFlow<AuthState> by lazy {
        observeAuthState { isLoggedIn, id, email, name ->
            authStateFlow.value = if (isLoggedIn) {
                AuthState.Authenticated(id, email, name)
            } else {
                AuthState.NotAuthenticated
            }
        }
        authStateFlow
    }
}