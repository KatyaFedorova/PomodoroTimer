package org.timer.main.domain.summary

interface SummaryGateway {
    suspend fun generate(systemPrompt: String, userPrompt: String): String
}

/** Used on platforms that have no local model yet (Android, iOS). */
class UnavailableSummaryGateway : SummaryGateway {
    override suspend fun generate(systemPrompt: String, userPrompt: String): String =
        throw UnsupportedOperationException("AI summary is only available on web for now")
}
