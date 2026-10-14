package org.timer.main.domain.summary

class FakeSummaryGateway : SummaryGateway {
    var response: String = "Great job today."
    var error: Throwable? = null
    var lastSystemPrompt: String? = null
    var lastUserPrompt: String? = null

    override suspend fun generate(systemPrompt: String, userPrompt: String): String {
        lastSystemPrompt = systemPrompt
        lastUserPrompt = userPrompt
        error?.let { throw it }
        return response
    }
}
