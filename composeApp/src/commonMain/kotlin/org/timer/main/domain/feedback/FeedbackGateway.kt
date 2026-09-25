package org.timer.main.domain.feedback

interface FeedbackGateway {
    suspend fun sendEmail(stars: Int, message: String)
}
