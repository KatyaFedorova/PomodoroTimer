package org.timer.main.domain.auth

import kotlinx.coroutines.flow.*

interface GetAuthStateUseCase {
    suspend operator fun invoke(): Flow<AuthState>
}

class DefaultGetAuthStateUseCase(private val authGateway: AuthGateway) : GetAuthStateUseCase {

    override suspend fun invoke(): Flow<AuthState> = authGateway.getAuthState()
}