package org.timer.main.domain.statistic

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import kotlin.test.*
import kotlin.time.Duration.Companion.hours

class StatisticUseCasesTest {

    private val exerciseEvents =
        listOf(EventType.EXERCISE_HIGH_ENERGY_COMPLETED, EventType.EXERCISE_LOW_ENERGY_COMPLETED)

    // ────────────────────────── Weekly delegates ──────────────────────────

    @Test
    fun `weekly pomodoro stats request only pomodoro events`() = runTest {
        val shared = FakeGetWeeklyEventStatsUseCase()
        DefaultGetWeeklyPomodoroStatsUseCase(shared).invoke(2)

        assertThat(shared.lastWeekOffset).isEqualTo(2)
        assertThat(shared.lastEventTypes).isEqualTo(listOf(EventType.POMODORO_COMPLETED))
    }

    @Test
    fun `weekly task stats request only task events`() = runTest {
        val shared = FakeGetWeeklyEventStatsUseCase()
        DefaultGetWeeklyTaskStatsUseCase(shared).invoke(0)

        assertThat(shared.lastEventTypes).isEqualTo(listOf(EventType.TASK_COMPLETED))
    }

    @Test
    fun `weekly exercise stats request both exercise events`() = runTest {
        val shared = FakeGetWeeklyEventStatsUseCase()
        DefaultGetWeeklyExerciseStatsUseCase(shared).invoke(0)

        assertThat(shared.lastEventTypes).isEqualTo(exerciseEvents)
    }

    // ────────────────────────── Monthly delegates ──────────────────────────

    @Test
    fun `monthly pomodoro stats request only pomodoro events`() = runTest {
        val shared = FakeGetMonthlyEventStatsUseCase()
        DefaultGetMonthlyPomodoroStatsUseCase(shared).invoke(-1)

        assertThat(shared.lastMonthOffset).isEqualTo(-1)
        assertThat(shared.lastEventTypes).isEqualTo(listOf(EventType.POMODORO_COMPLETED))
    }

    @Test
    fun `monthly task stats request only task events`() = runTest {
        val shared = FakeGetMonthlyEventStatsUseCase()
        DefaultGetMonthlyTaskStatsUseCase(shared).invoke(0)

        assertThat(shared.lastEventTypes).isEqualTo(listOf(EventType.TASK_COMPLETED))
    }

    @Test
    fun `monthly exercise stats request both exercise events`() = runTest {
        val shared = FakeGetMonthlyEventStatsUseCase()
        DefaultGetMonthlyExerciseStatsUseCase(shared).invoke(0)

        assertThat(shared.lastEventTypes).isEqualTo(exerciseEvents)
    }

    // ────────────────────────── Monthly core bucketing ──────────────────────────

    @Test
    fun `monthly event stats count matching events within the month`() = runTest {
        // Bucket in the system default zone (what the use case uses) at midday, so the
        // counts hold regardless of the runner's time zone (e.g. a UTC CI machine).
        val tz = TimeZone.currentSystemDefault()
        fun midday(date: LocalDate) = date.atTime(12, 0).toInstant(tz).toEpochMilliseconds()
        val clock = FakeSystemClock().apply {
            currentTime = midday(LocalDate(2025, 9, 15))
            currentTimeZone = tz.id
        }
        val repo = FakeStatisticsEventGateway().apply {
            eventsInRange = listOf(
                Event(EventType.POMODORO_COMPLETED, emptyMap(), midday(LocalDate(2025, 9, 1)), tz.id),
                Event(EventType.POMODORO_COMPLETED, emptyMap(), midday(LocalDate(2025, 9, 10)), tz.id),
            )
        }

        val stats = DefaultGetMonthlyEventStatsUseCase(repo, clock)
            .invoke(0, listOf(EventType.POMODORO_COMPLETED)).first()

        assertThat(stats.monthStart).isEqualTo(LocalDate(2025, 9, 1))
        assertThat(stats.daily.sumOf { it.count }).isEqualTo(2)
    }
}
