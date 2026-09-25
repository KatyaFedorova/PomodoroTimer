package org.timer.main.domain.feedback

import com.varabyte.truthish.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class FeedbackUseCaseTest {

    @Test
    fun `sending feedback forwards the rating and message to the gateway`() = runTest {
        val gateway = FakeFeedbackGateway()

        DefaultSendFeedbackUseCase(gateway).invoke(4, "great app")

        assertThat(gateway.sentEmail).isEqualTo(4 to "great app")
    }
}
