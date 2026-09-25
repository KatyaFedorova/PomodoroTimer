package org.timer.main.domain.auth

interface LogoutUseCase {
    suspend operator fun invoke()
}

class DefaultLogoutUseCase(private val authGateway: AuthGateway) : LogoutUseCase {

    override suspend fun invoke() {
        authGateway.logout()
    }
}