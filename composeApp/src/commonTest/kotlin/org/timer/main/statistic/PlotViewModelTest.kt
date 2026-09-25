package org.timer.main.statistic

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import org.timer.main.domain.statistic.*
import org.timer.main.statistic.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class PlotViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepo: FakeStatisticsEventGateway
    private lateinit var fakeClock: FakeSystemClock
    private lateinit var viewModel: PlotViewModel

    private val tz = TimeZone.of("Europe/Warsaw")
    private val monday = LocalDate(2025, 9, 22) // понедельник

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeStatisticsEventGateway()
        fakeClock = FakeSystemClock().apply {
            currentTime = monday.atStartOfDayIn(tz).toEpochMilliseconds()
            currentTimeZone = tz.id
        }
        val weeklyEventStats = DefaultGetWeeklyEventStatsUseCase(fakeRepo, fakeClock)
        val monthlyEventStats = DefaultGetMonthlyEventStatsUseCase(fakeRepo, fakeClock)
        viewModel = PlotViewModel(
            getWeeklyPomodoroStatsUseCase = DefaultGetWeeklyPomodoroStatsUseCase(weeklyEventStats),
            getMonthlyPomodoroStatsUseCase = DefaultGetMonthlyPomodoroStatsUseCase(monthlyEventStats),
            getWeeklyExerciseStatsUseCase = DefaultGetWeeklyExerciseStatsUseCase(weeklyEventStats),
            getMonthlyExerciseStatsUseCase = DefaultGetMonthlyExerciseStatsUseCase(monthlyEventStats),
            getWeeklyTaskStatsUseCase = DefaultGetWeeklyTaskStatsUseCase(weeklyEventStats),
            getMonthlyTaskStatsUseCase = DefaultGetMonthlyTaskStatsUseCase(monthlyEventStats),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ────────────────────────────── Initial load ──────────────────────────────

    @Test
    fun `initial load fills the current pomodoro week and month`() = runTest {
        advanceUntilIdle()
        val state = viewModel.viewState.value

        assertThat(state.pomodoroWeekOffset).isEqualTo(0)
        // current week runs Sunday 21.09 – Saturday 27.09
        assertThat(state.pomodoroWeekStart).isEqualTo(LocalDate(2025, 9, 21))
        assertThat(state.pomodoroWeekEnd).isEqualTo(LocalDate(2025, 9, 27))
        assertThat(state.pomodoroWeekly.size).isEqualTo(7)

        assertThat(state.pomodoroMonthOffset).isEqualTo(0)
        assertThat(state.pomodoroMonthStart).isNotNull()
        assertThat(state.pomodoroMonthly.isEmpty()).isFalse()
    }

    @Test
    fun `initial load fills the current task week and month`() = runTest {
        advanceUntilIdle()
        val state = viewModel.viewState.value

        assertThat(state.taskWeekOffset).isEqualTo(0)
        assertThat(state.taskWeekStart).isEqualTo(LocalDate(2025, 9, 21))
        assertThat(state.taskWeekEnd).isEqualTo(LocalDate(2025, 9, 27))
        assertThat(state.taskWeekly.size).isEqualTo(7)

        assertThat(state.taskMonthOffset).isEqualTo(0)
        assertThat(state.taskMonthStart).isNotNull()
    }

    @Test
    fun `initial load fills the current exercise week and month`() = runTest {
        advanceUntilIdle()
        val state = viewModel.viewState.value

        assertThat(state.exerciseWeekOffset).isEqualTo(0)
        assertThat(state.exerciseWeekStart).isEqualTo(LocalDate(2025, 9, 21))
        assertThat(state.exerciseWeekEnd).isEqualTo(LocalDate(2025, 9, 27))
        assertThat(state.exerciseWeekly.size).isEqualTo(7)

        assertThat(state.exerciseMonthOffset).isEqualTo(0)
        assertThat(state.exerciseMonthStart).isNotNull()
    }

    // ────────────────────────────── Pomodoro navigation ──────────────────────────────

    @Test
    fun `previous pomodoro week shows the prior week and leaves other sections alone`() = runTest {
        advanceUntilIdle()

        viewModel.onPrevPomodoroWeek()
        advanceUntilIdle()
        val state = viewModel.viewState.value

        // previous week runs Sunday 14.09 – Saturday 20.09
        assertThat(state.pomodoroWeekOffset).isEqualTo(-1)
        assertThat(state.pomodoroWeekStart).isEqualTo(LocalDate(2025, 9, 14))
        assertThat(state.pomodoroWeekEnd).isEqualTo(LocalDate(2025, 9, 20))
        // unrelated sections untouched
        assertThat(state.pomodoroMonthOffset).isEqualTo(0)
        assertThat(state.taskWeekOffset).isEqualTo(0)
        assertThat(state.exerciseWeekOffset).isEqualTo(0)
    }

    @Test
    fun `next pomodoro week shows the following week`() = runTest {
        advanceUntilIdle()

        viewModel.onNextPomodoroWeek()
        advanceUntilIdle()
        val state = viewModel.viewState.value

        // next week runs Sunday 28.09 – Saturday 04.10
        assertThat(state.pomodoroWeekOffset).isEqualTo(1)
        assertThat(state.pomodoroWeekStart).isEqualTo(LocalDate(2025, 9, 28))
        assertThat(state.pomodoroWeekEnd).isEqualTo(LocalDate(2025, 10, 4))
    }

    @Test
    fun `previous pomodoro month moves only the pomodoro month offset`() = runTest {
        advanceUntilIdle()

        viewModel.onPrevPomodoroMonth()
        advanceUntilIdle()
        val state = viewModel.viewState.value

        assertThat(state.pomodoroMonthOffset).isEqualTo(-1)
        assertThat(state.pomodoroWeekOffset).isEqualTo(0)
    }

    // ────────────────────────────── Task navigation ──────────────────────────────

    @Test
    fun `next task week moves only the task week offset`() = runTest {
        advanceUntilIdle()

        viewModel.onNextTaskWeek()
        advanceUntilIdle()
        val state = viewModel.viewState.value

        assertThat(state.taskWeekOffset).isEqualTo(1)
        assertThat(state.pomodoroWeekOffset).isEqualTo(0)
        assertThat(state.exerciseWeekOffset).isEqualTo(0)
    }

    @Test
    fun `previous task month moves only the task month offset`() = runTest {
        advanceUntilIdle()

        viewModel.onPrevTaskMonth()
        advanceUntilIdle()
        val state = viewModel.viewState.value

        assertThat(state.taskMonthOffset).isEqualTo(-1)
        assertThat(state.pomodoroMonthOffset).isEqualTo(0)
    }

    // ────────────────────────────── Exercise navigation ──────────────────────────────

    @Test
    fun `next exercise week moves only the exercise week offset`() = runTest {
        advanceUntilIdle()

        viewModel.onNextExerciseWeek()
        advanceUntilIdle()
        val state = viewModel.viewState.value

        assertThat(state.exerciseWeekOffset).isEqualTo(1)
        assertThat(state.pomodoroWeekOffset).isEqualTo(0)
        assertThat(state.taskWeekOffset).isEqualTo(0)
    }

    @Test
    fun `previous exercise month moves only the exercise month offset`() = runTest {
        advanceUntilIdle()

        viewModel.onPrevExerciseMonth()
        advanceUntilIdle()
        val state = viewModel.viewState.value

        assertThat(state.exerciseMonthOffset).isEqualTo(-1)
        assertThat(state.pomodoroMonthOffset).isEqualTo(0)
    }
}
