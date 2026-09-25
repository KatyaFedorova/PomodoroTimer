package org.timer.main.domain.feedback

class FakeSendFeedbackUseCase : SendFeedbackUseCase {
    var sentFeedback: Pair<Int, String>? = null

    override suspend fun invoke(stars: Int, message: String) {
        sentFeedback = Pair(stars, message)
    }
}
