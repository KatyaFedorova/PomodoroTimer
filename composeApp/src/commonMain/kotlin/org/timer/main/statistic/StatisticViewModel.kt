package org.timer.main.statistic

import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import org.timer.main.domain.game.*
import pomodorotimer.composeapp.generated.resources.*

private const val TOTAL_FISHES_ON_IMAGE = 12

class StatisticViewModel(
    private val getFeedingDatesUseCase: GetFeedingDatesUseCase,
    private val getFishesNumberUseCaseUseCase: GetFishesNumberUseCaseUseCase

) : ViewModel() {
    private val _viewState = MutableStateFlow(StatisticViewState())
    val viewState: StateFlow<StatisticViewState> = _viewState.asStateFlow()

    init {
        getFeedingDatesUseCase().onEach { feedingDates ->
            _viewState.update { it.copy(feedingDates = feedingDates) }
        }.launchIn(viewModelScope)

        getFishesNumberUseCaseUseCase().onEach { fishesNumber ->
            updateFishNumberViewState(fishesNumber)
        }.launchIn(viewModelScope)
    }

    private fun updateFishNumberViewState(fishesNumber: Int) {
        _viewState.update {
            it.copy(
                totalFishes = fishesNumber,
                showFishNumberText = fishesNumber > TOTAL_FISHES_ON_IMAGE
            )
        }
        when (fishesNumber) {
            0 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes0) }
            1 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes1) }
            2 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes2) }
            3 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes3) }
            4 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes4) }
            5 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes5) }
            6 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes6) }
            7 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes7) }
            8 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes8) }
            9 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes9) }
            10 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes10) }
            11 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes11) }
            12 -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes12) }
            else -> _viewState.update { it.copy(fishImageRes = Res.drawable.fishes0) }
        }
    }
}