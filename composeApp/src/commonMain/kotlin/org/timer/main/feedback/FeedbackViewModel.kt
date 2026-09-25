package org.timer.main.feedback

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.timer.main.domain.feedback.*

class FeedbackViewModel(
    private val sendFeedbackUseCase: SendFeedbackUseCase,
) : ViewModel() {

    private val _viewState = MutableStateFlow(FeedbackViewState())
    val viewState: StateFlow<FeedbackViewState> = _viewState.asStateFlow()

    fun onSendFeedbackClick() = viewModelScope.launch {
        sendFeedbackUseCase(viewState.value.stars, viewState.value.massage)
        _viewState.update { it.copy(isSuccess = true) }
    }

    fun onMassageChanged(message: String) {
        _viewState.update { it.copy(massage = message) }
    }

    fun onStarClick(stars: Int) {
        _viewState.update { it.copy(stars = stars) }
    }

}

