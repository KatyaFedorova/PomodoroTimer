package org.timer.main.domain.summary

import kotlinx.coroutines.await
import kotlin.js.Promise

external interface AiSummaryResult : JsAny {
    val success: Boolean
    val text: String?
    val error: String?
}

@JsName("aiSummaryBridge")
external object AiSummaryBridge {
    fun generate(userPrompt: String): Promise<AiSummaryResult>
}

/**
 * Production gateway: the generateDailySummary Cloud Function (functions/src/index.ts) owns the
 * system prompt and the model key, so only the user prompt is sent from the browser.
 */
class FirebaseSummaryGateway : SummaryGateway {

    override suspend fun generate(systemPrompt: String, userPrompt: String): String {
        val result: AiSummaryResult = AiSummaryBridge.generate(userPrompt).await()
        if (!result.success) throw Exception(result.error ?: "Summary request failed")
        return result.text.orEmpty()
    }
}
