package org.timer.main.domain.feedback

import kotlinx.coroutines.*
import kotlin.js.Promise

external interface JsResult : JsAny {
    val success: Boolean
    val error: String?
}

@JsName("myAppJsFirebase")
private external object MyAppJsFirebaseFeedbackBridge {
    fun sendFeedback(stars: Int, message: String): Promise<JsResult>
}

actual class FirebaseFeedbackGateway actual constructor() : FeedbackGateway {

    override suspend fun sendEmail(stars: Int, message: String) {
        val result: JsResult = MyAppJsFirebaseFeedbackBridge.sendFeedback(stars, message).await()
    }

}