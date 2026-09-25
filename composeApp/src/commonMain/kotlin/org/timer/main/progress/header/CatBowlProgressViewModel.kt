package org.timer.main.progress.header

import androidx.lifecycle.*
import com.diamondedge.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.timer.main.domain.game.*

class CatBowlProgressViewModel(
    private val getCatBowlFillingProgressUseCase: GetCatBowlFillingProgressUseCase,
    private val resetCatBowlFillingProgressUseCase: ResetCatBowlFillingProgressUseCase,
) : ViewModel() {


    private val _viewState = MutableStateFlow(TimerViewState())
    val viewState: StateFlow<TimerViewState> = _viewState.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>(replay = 1)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    init {
        getCatBowlFillingProgressUseCase()
            .onEach { fillingProgress ->
                val isBowlFull = fillingProgress >= GameGateway.TOTAL_PROGRESS
                val progressFraction = fillingProgress / GameGateway.TOTAL_PROGRESS.toFloat()
                _viewState.update {
                    it.copy(
                        fillingProgress = progressFraction,
                        isBowlFull = isBowlFull
                    )
                }

                if (fillingProgress > 0) {
                    val percent = (progressFraction * 100).toInt()
                    _toastMessages.emit("Bowl is filled for $percent%")
                }
            }
            .launchIn(viewModelScope)

    }

    fun onResume() = viewModelScope.launch {
        resetCatBowlFillingProgressUseCase()
    }

    companion object {
        val log = logging()
    }
}