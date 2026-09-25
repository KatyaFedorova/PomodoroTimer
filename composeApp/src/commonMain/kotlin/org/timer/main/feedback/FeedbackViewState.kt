package org.timer.main.feedback

data class FeedbackViewState(
    val stars: Int = 0,
    val massage: String = "",
    val isSuccess: Boolean = false
) {
    val isSendEnabled: Boolean
        get() = massage.isNotBlank() || stars > 0
}

