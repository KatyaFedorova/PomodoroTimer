package org.timer.main

import android.content.*
import androidx.activity.*
import androidx.activity.result.*
import androidx.activity.result.contract.*
import com.diamondedge.logging.*
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.*
import com.google.firebase.analytics.*
import com.google.firebase.analytics.ktx.*
import com.google.firebase.auth.*
import com.google.firebase.ktx.*
import org.timer.R
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.emptyMap
import kotlin.collections.iterator
import kotlin.collections.mapOf

object FirebaseAuthBridge {

    private lateinit var auth: FirebaseAuth
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var signInIntent: Intent
    private lateinit var appContext: Context

    private var onSuccessCallback: (() -> Unit)? = null
    private var onErrorCallback: ((Throwable) -> Unit)? = null

    private var analytics: FirebaseAnalytics? = null

    fun init(activity: ComponentActivity) {
        analytics = Firebase.analytics
        auth = FirebaseAuth.getInstance()
        appContext = activity.applicationContext
        logEvent("init")
        signInLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                logEvent(
                    "auth_google_sign_in_result",
                    mapOf("resultCode" to result.resultCode.toString())
                )

                try {
                    handleGoogleResult(result.data)
                } catch (e: Exception) {
                    logEvent(
                        "auth_google_sign_in_unexpected_error",
                        mapOf("message" to e.message.orEmpty())
                    )
                    onErrorCallback?.invoke(e)
                    clearCallbacks()
                }
            }

        val webClientId = appContext.getString(R.string.default_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        signInIntent = googleSignInClient.signInIntent
    }

    fun signInWithGoogle(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        onSuccessCallback = onSuccess
        onErrorCallback = onError

        signInLauncher.launch(signInIntent)
    }

    fun signOut() {
        auth.signOut()
        logEvent("auth_user_sign_out")
    }

    fun observeAuthState(listener: (Boolean, String, String, String?) -> Unit) {
        auth.addAuthStateListener {
            listener(
                it.currentUser != null,
                it.currentUser?.uid ?: "",
                it.currentUser?.email ?: "",
                it.currentUser?.displayName
            )
        }
    }

    private fun handleGoogleResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            logEvent("auth_google_sign_in_success", mapOf("email" to (account?.email ?: "")))
            signInWithFirebase(account?.idToken)
        } catch (e: ApiException) {
            when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> {
                    logEvent("auth_google_sign_in_cancelled_by_user")
                    onErrorCallback?.invoke(Exception("Sign-in canceled by user"))
                }

                GoogleSignInStatusCodes.SIGN_IN_FAILED -> {
                    logEvent(
                        "auth_google_sign_in_failed",
                        mapOf("statusCode" to e.statusCode.toString())
                    )
                    onErrorCallback?.invoke(Exception("Sign-in failed"))
                }

                else -> {
                    logEvent(
                        "auth_google_sign_in_error",
                        mapOf("statusCode" to e.statusCode.toString())
                    )
                    onErrorCallback?.invoke(e)
                }
            }
            clearCallbacks()
        } catch (e: Exception) {
            logEvent(
                "auth_google_sign_in_unexpected_error",
                mapOf("message" to e.message.orEmpty())
            )
            onErrorCallback?.invoke(e)
            clearCallbacks()
        }
    }

    private fun signInWithFirebase(idToken: String?) {
        if (idToken.isNullOrEmpty()) {
            logEvent("auth_google_sign_in_missing_token")
            onErrorCallback?.invoke(IllegalStateException("Missing Google ID Token"))
            clearCallbacks()
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                logEvent("auth_firebase_sign_in_success")
                onSuccessCallback?.invoke()
                clearCallbacks()
            }
            .addOnFailureListener {
                val safeMessage = it.message.orEmpty()
                logEvent("auth_firebase_sign_in_failed", mapOf("message" to safeMessage))

                onErrorCallback?.invoke(it)
                clearCallbacks()
            }
    }

    private fun clearCallbacks() {
        onSuccessCallback = null
        onErrorCallback = null
    }

    private fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
        val bundle = android.os.Bundle()

        for ((k, v) in params) {
            val safeValue = if (v.length > 40) v.take(37) + "..." else v
            bundle.putString(k, safeValue)
        }

        analytics?.logEvent(name, bundle)
        logging("analytics").d { "logEvent: $name $params" }
    }
}
