package org.timer.main.feedback

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.feedback.*
import org.timer.main.feedback.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class FeedbackViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var sendFeedbackUseCase: FakeSendFeedbackUseCase
    private lateinit var viewModel: FeedbackViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sendFeedbackUseCase = FakeSendFeedbackUseCase()
        viewModel = FeedbackViewModel(sendFeedbackUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should show no stars empty message and sending disabled`() = runTest {
        val state = viewModel.viewState.first()
        assertThat(state.stars).isEqualTo(0)
        assertThat(state.massage).isEqualTo("")
        assertThat(state.isSendEnabled).isFalse()
        assertThat(state.isSuccess).isFalse()
    }

    @Test
    fun `typing a message should enable sending`() = runTest {
        viewModel.onMassageChanged("Great app")
        val state = viewModel.viewState.value
        assertThat(state.massage).isEqualTo("Great app")
        assertThat(state.isSendEnabled).isTrue()
    }

    @Test
    fun `picking a star rating should enable sending`() = runTest {
        viewModel.onStarClick(4)
        val state = viewModel.viewState.value
        assertThat(state.stars).isEqualTo(4)
        assertThat(state.isSendEnabled).isTrue()
    }

    @Test
    fun `sending feedback should pass the rating and message to the use case`() = runTest {
        viewModel.onStarClick(5)
        viewModel.onMassageChanged("Loving it")
        viewModel.onSendFeedbackClick()
        advanceUntilIdle()
        assertThat(sendFeedbackUseCase.sentFeedback).isEqualTo(5 to "Loving it")
    }

    @Test
    fun `sending feedback should mark the screen as successful`() = runTest {
        viewModel.onSendFeedbackClick()
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.isSuccess).isTrue()
    }
}
