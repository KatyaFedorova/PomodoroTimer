package org.timer.main.domain.auth

import com.russhwolf.settings.*
import dev.gitlive.firebase.*
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.timer.main.*
import kotlin.coroutines.*


actual class DefaultAuthGateway : AuthGateway {

    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Loading)
    private val setting = Settings()

    init {
        FirebaseAuthBridge.observeAuthState { isSignedIn, id, email, name ->
            val state =
                if (isSignedIn) AuthState.Authenticated(
                    id,
                    email,
                    name
                ) else AuthState.NotAuthenticated
            authStateFlow.tryEmit(state)
        }
    }

    override suspend fun loginWithGoogle() {
        suspendCancellableCoroutine { cont ->
            FirebaseAuthBridge.signInWithGoogle(
                onSuccess = {
                    cont.resume(Unit)
                },
                onError = { error ->
                    cont.resumeWithException(error)
                }
            )
        }
    }

    override fun getAuthState(): Flow<AuthState> = authStateFlow

    override suspend fun logout() {
        FirebaseAuthBridge.signOut()
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
        val actionCodeSettings = if (BuildConfig.DEBUG) {
            ActionCodeSettings(
                url = "https://tomatopawsdev.web.app",
                androidPackageName = AndroidPackageName(
                    packageName = "org.timer.dev",
                    installIfNotAvailable = true
                ),
                canHandleCodeInApp = true,
                iOSBundleId = "org.timer.dev",
                dynamicLinkDomain = "tomatopawsdev.web.app"
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
                dynamicLinkDomain = "kittidoro-timer.web.app",
                )
        }
        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
        setting.putString("email", email)
    }
    // push re
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
}