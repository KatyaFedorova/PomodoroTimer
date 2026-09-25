package org.timer.main.domain.feedback

interface SendFeedbackUseCase {
    suspend operator fun invoke(stars: Int, message: String)
}

class DefaultSendFeedbackUseCase(
    private val feedbackGateway: FeedbackGateway
) : SendFeedbackUseCase {

    override suspend fun invoke(stars: Int, message: String) {
        feedbackGateway.sendEmail(stars, message)
    }
}

