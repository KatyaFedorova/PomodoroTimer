package org.timer.main.domain.auth

import com.diamondedge.logging.*
import kotlinx.coroutines.flow.*

interface SendMagicLinkUseCase {
    suspend operator fun invoke(email: String)
}

class DefaultSendMagicLinkUseCase(private val authGateway: AuthGateway) : SendMagicLinkUseCase {

    override suspend fun invoke(email: String) {
        authGateway.sendMagicLink(email)
    }
}