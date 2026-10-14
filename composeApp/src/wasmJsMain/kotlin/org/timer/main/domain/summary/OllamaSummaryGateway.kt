package org.timer.main.domain.summary

import kotlinx.coroutines.await
import kotlin.js.Promise

external interface OllamaResult : JsAny {
    val success: Boolean
    val text: String?
    val error: String?
}

@JsName("ollamaBridge")
external object OllamaBridge {
    fun generate(model: String, systemPrompt: String, userPrompt: String): Promise<OllamaResult>
}

class OllamaSummaryGateway(
    private val model: String = DEFAULT_MODEL,
) : SummaryGateway {

    override suspend fun generate(systemPrompt: String, userPrompt: String): String {
        val result: OllamaResult = OllamaBridge.generate(model, systemPrompt, userPrompt).await()
        if (!result.success) throw Exception(result.error ?: "Ollama request failed")
        return result.text.orEmpty()
    }

    companion object {
        const val DEFAULT_MODEL = "llama3.2:3b"
    }
}
