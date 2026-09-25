package org.timer.main.domain.auth

import com.diamondedge.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.timer.main.domain.feedback.JsResult
import kotlin.js.Promise

private external interface FirebaseJsBridge {
    fun signInWithGooglePopup(): Promise<JsAny?>
    fun signOut(): Promise<JsAny?>
    fun observeAuthState(callback: (isSignedIn: Boolean, uid: String?, email: String?, name: String?) -> Unit): () -> Unit
    fun createUser(id: String, email: String, name: String?): Promise<JsAny?>
    fun sendLoginLink(email: String): Promise<JsAny?>
    fun completeLoginWithLink(url: String): Promise<JsAny?>

}

external interface JsResult : JsAny {
    val success: Boolean
    val error: String?
}


@JsName("window.myAppJsFirebase")
private external val myAppJsFirebase: FirebaseJsBridge?

actual class DefaultAuthGateway : AuthGateway {

    private val _currentAuthState = MutableStateFlow<AuthState>(AuthState.Loading)

    init {
        observeFirebaseAuthState()
    }

    private fun observeFirebaseAuthState() {
        try {
            val bridge = getFirebaseJs()
            bridge.observeAuthState { isSignedIn, uid, email, name ->
                _currentAuthState.value =
                    if (isSignedIn) AuthState.Authenticated(
                        uid!!,
                        email!!,
                        name
                    ) else AuthState.NotAuthenticated
            }
        } catch (e: Throwable) {
            val errorMessage = e.message ?: "Unknown error setting up Firebase AuthState observer"
            logging.error { "Error setting up Firebase AuthState observer: $errorMessage" }
            _currentAuthState.value = AuthState.NotAuthenticated
        }
    }

    private fun getFirebaseJs(): FirebaseJsBridge {
        return myAppJsFirebase
            ?: throw IllegalStateException("Firebase JS bridge (window.myAppJsFirebase) is not available.")
    }

    override suspend fun loginWithGoogle() {
        return try {
            val responseJs: JsAny? = getFirebaseJs().signInWithGooglePopup().await()
        } catch (e: Throwable) {
            val errorMessage = e.message ?: "JavaScript error during Google login"
            logging.error { errorMessage }
        }
    }

    override fun getAuthState(): Flow<AuthState> = _currentAuthState

    override suspend fun logout() {
        getFirebaseJs().signOut()
    }

    override suspend fun createUser(id: String, email: String, name: String?) {
        logging.info { "createUser: $id, $email, $name" }
        getFirebaseJs().createUser(id, email, name).await<JsAny?>()
    }

    override suspend fun sendMagicLink(email: String) {
        try {
            getFirebaseJs().sendLoginLink(email).await<JsAny?>()
            logging.info { "Magic link sent to $email" }
        } catch (e: Throwable) {
            val errorMessage = e.message ?: "JavaScript error during sendMagicLink"
            logging.error { errorMessage }
            throw e
        }
    }

    override suspend fun completeLoginWithLink(url: String): MagicLinkResult {
        return try {
            val result: JsResult = getFirebaseJs().completeLoginWithLink(url).await()
            if (result.success) {
                MagicLinkResult.Success
            } else {
                MagicLinkResult.Error(result.error ?: "Unknown error during magic link sign-in")
            }
        } catch (e: Throwable) {
            MagicLinkResult.Error(e.message ?: "Unknown error during magic link sign-in")
        }

    }

    companion object {
        val logging = logging()
    }
}

