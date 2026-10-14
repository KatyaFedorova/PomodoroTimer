package org.timer.main.summary

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.summary.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class DailySummaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val activity = DailyActivity(3, 1, 1, 1, 0, 0, emptyList())
    private val activityFlow = MutableStateFlow(activity)
    private val summaryGateway = FakeSummaryGateway()
    private lateinit var viewModel: DailySummaryViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DailySummaryViewModel(
            getTodayActivityUseCase = object : GetTodayActivityUseCase {
                override fun invoke(): Flow<DailyActivity> = activityFlow
            },
            generateDailySummaryUseCase = DefaultGenerateDailySummaryUseCase(summaryGateway),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `dialog is hidden initially`() {
        assertThat(viewModel.viewState.value.isDialogVisible).isFalse()
    }

    @Test
    fun `clicking summary opens dialog with stats and generated summary`() = runTest {
        summaryGateway.response = "You did great."

        viewModel.onSummaryClick()
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.isDialogVisible).isTrue()
        assertThat(state.isLoading).isFalse()
        assertThat(state.activity).isEqualTo(activity)
        assertThat(state.summary).isEqualTo("You did great.")
    }

    @Test
    fun `model failure shows error`() = runTest {
        summaryGateway.error = Exception("connection refused")

        viewModel.onSummaryClick()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.error).isEqualTo("connection refused")
        assertThat(viewModel.viewState.value.summary).isNull()
    }

    @Test
    fun `stats update live and summary is regenerated when a task is completed`() = runTest {
        viewModel.onSummaryClick()
        advanceUntilIdle()

        summaryGateway.response = "Nice, two tasks done."
        val updated = activity.copy(tasksCompleted = 2)
        activityFlow.value = updated
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.activity).isEqualTo(updated)
        assertThat(state.summary).isEqualTo("Nice, two tasks done.")
        assertThat(summaryGateway.lastUserPrompt!!).contains("- 2 tasks completed")
    }

    @Test
    fun `dismiss hides dialog`() = runTest {
        viewModel.onSummaryClick()
        advanceUntilIdle()
        viewModel.onDismiss()

        assertThat(viewModel.viewState.value.isDialogVisible).isFalse()
    }
}
