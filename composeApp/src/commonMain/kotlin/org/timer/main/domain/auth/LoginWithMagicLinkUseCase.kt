package org.timer.main.domain.auth

import kotlinx.coroutines.flow.*

interface LoginWithMagicLinkUseCase {
    suspend operator fun invoke(url: String): MagicLinkResult
}

sealed class MagicLinkResult {
    data object Success : MagicLinkResult()
    data class Error(val message: String) : MagicLinkResult()
}

class DefaultMagicLinkLoginUseCase(private val authGateway: AuthGateway) :
    LoginWithMagicLinkUseCase {
    override suspend fun invoke(url: String): MagicLinkResult {
        when (val result = authGateway.completeLoginWithLink(url)) {
            is MagicLinkResult.Success -> {
                val authState = authGateway.getAuthState().first()
                if (authState is AuthState.Authenticated) {
                    authGateway.createUser(authState.id, authState.email, authState.name)
                }
                return result
            }

            is MagicLinkResult.Error -> {
                return result
            }
        }
    }
}