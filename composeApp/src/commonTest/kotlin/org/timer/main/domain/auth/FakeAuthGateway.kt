package org.timer.main.domain.auth

import kotlinx.coroutines.flow.*
import org.timer.main.domain.auth.*

class FakeAuthGateway : AuthGateway {
    val flow = MutableStateFlow<AuthState>(AuthState.Loading)
    var createdUser: Pair<String, String>? = null
    var sentMagicLinkEmail: String? = null

    override suspend fun loginWithGoogle() {
        flow.emit(AuthState.Authenticated("user_id", "email", null))
    }

    override fun getAuthState(): Flow<AuthState> = flow

    override suspend fun logout() {
        flow.emit(AuthState.NotAuthenticated)
    }

    override suspend fun createUser(id: String, email: String, name: String?) {
        createdUser = Pair(id, email)
    }

    override suspend fun sendMagicLink(email: String) {
        sentMagicLinkEmail = email
    }

    override suspend fun completeLoginWithLink(url: String): MagicLinkResult {
        flow.emit(AuthState.Authenticated("user_id", "email", null))
        return MagicLinkResult.Success
    }
}
