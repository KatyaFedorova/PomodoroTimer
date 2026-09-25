package org.timer.main.domain.auth

import kotlinx.coroutines.flow.*

interface AuthGateway {
    suspend fun loginWithGoogle()
    fun getAuthState(): Flow<AuthState>
    suspend fun logout()
    suspend fun createUser(id: String, email: String,name: String?)
    suspend fun sendMagicLink(email: String)
    suspend fun completeLoginWithLink(url: String): MagicLinkResult
}


sealed class AuthState {
    data class Authenticated(val id: String, val email: String,val name: String?) : AuthState()
    data object Loading : AuthState()
    data object NotAuthenticated : AuthState()
}