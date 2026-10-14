package org.timer.main.summary

import androidx.lifecycle.*
import com.diamondedge.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.timer.main.domain.summary.*

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class DailySummaryViewModel(
    private val getTodayActivityUseCase: GetTodayActivityUseCase,
    private val generateDailySummaryUseCase: GenerateDailySummaryUseCase,
) : ViewModel() {

    private val _viewState = MutableStateFlow(DailySummaryViewState())
    val viewState: StateFlow<DailySummaryViewState> = _viewState.asStateFlow()

    private var job: Job? = null

    fun onSummaryClick() {
        _viewState.update { it.copy(isDialogVisible = true) }
        observeTodayActivity()
    }

    fun onRetryClick() = observeTodayActivity()

    fun onDismiss() {
        job?.cancel()
        _viewState.update { it.copy(isDialogVisible = false, isLoading = false) }
    }

    /**
     * Stats update live while the dialog is open. The summary is (re)generated once the stats
     * settle, so a just-ticked task that Firebase confirms a moment later is included.
     */
    private fun observeTodayActivity() {
        job?.cancel()
        job = viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, error = null, summary = null) }
            try {
                getTodayActivityUseCase()
                    .distinctUntilChanged()
                    .onEach { activity ->
                        _viewState.update { it.copy(activity = activity, isLoading = true, error = null, summary = null) }
                    }
                    .debounce(SETTLE_MILLIS)
                    .collectLatest { activity -> generateSummary(activity) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                showError(e)
            }
        }
    }

    private suspend fun generateSummary(activity: DailyActivity) {
        try {
            val summary = generateDailySummaryUseCase(activity)
            _viewState.update { it.copy(isLoading = false, summary = summary) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            showError(e)
        }
    }

    private fun showError(e: Throwable) {
        logging().e { "[DailySummaryViewModel] ${e.message}" }
        _viewState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
    }

    companion object {
        const val SETTLE_MILLIS = 1_500L
    }
}

data class DailySummaryViewState(
    val isDialogVisible: Boolean = false,
    val isLoading: Boolean = false,
    val activity: DailyActivity? = null,
    val summary: String? = null,
    val error: String? = null,
)
