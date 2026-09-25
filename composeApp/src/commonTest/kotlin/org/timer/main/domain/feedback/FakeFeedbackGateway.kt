package org.timer.main.domain.feedback

class FakeFeedbackGateway : FeedbackGateway {
    var sentEmail: Pair<Int, String>? = null

    override suspend fun sendEmail(stars: Int, message: String) {
        sentEmail = Pair(stars, message)
    }
}
