package org.timer.main.domain.auth

import kotlinx.coroutines.flow.*

interface LoginWithGoogleUseCase {
    suspend operator fun invoke()
}

class DefaultLoginWithGoogleUseCase(private val authGateway: AuthGateway) : LoginWithGoogleUseCase {

    override suspend fun invoke() {
        authGateway.loginWithGoogle()
        val authState = authGateway.getAuthState().first()
        if (authState is AuthState.Authenticated) {
            authGateway.createUser(authState.id, authState.email,authState.name)
        }
    }
}