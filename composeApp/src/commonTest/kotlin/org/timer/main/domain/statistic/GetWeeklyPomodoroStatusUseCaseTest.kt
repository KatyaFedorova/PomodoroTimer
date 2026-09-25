package org.timer.main.domain.statistic

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import org.timer.main.domain.statistic.*
import kotlin.test.*

class GetWeeklyPomodoroStatusUseCaseTest {

    private lateinit var getWeeklyPomodoroStatusUseCase: DefaultGetWeeklyPomodoroStatsUseCase
    private lateinit var fakeStatisticsEventGateway: FakeStatisticsEventGateway
    private lateinit var fakeSystemClock: FakeSystemClock

    // The use case buckets events using the system default time zone, so the test builds its
    // timestamps in that same zone and at midday — staying clear of date boundaries — so the
    // day counts hold regardless of where it runs (e.g. a UTC CI runner).
    private val tz = TimeZone.currentSystemDefault()

    @BeforeTest
    fun setUp() {
        fakeStatisticsEventGateway = FakeStatisticsEventGateway()
        fakeSystemClock = FakeSystemClock()
        getWeeklyPomodoroStatusUseCase =
            DefaultGetWeeklyPomodoroStatsUseCase(
                DefaultGetWeeklyEventStatsUseCase(fakeStatisticsEventGateway, fakeSystemClock)
            )
    }

    private fun LocalDate.atMidday() = atTime(12, 0).toInstant(tz).toEpochMilliseconds()

    private fun pomodoroOn(date: LocalDate) =
        Event(EventType.POMODORO_COMPLETED, emptyMap(), date.atMidday(), tz.id)

    @Test
    fun `counts pomodoros per day in current week`() = runTest {
        // Monday 22.09.2025 -> week runs Sunday 21 – Saturday 27
        fakeSystemClock.currentTime = LocalDate(2025, 9, 22).atMidday()

        fakeStatisticsEventGateway.eventsInRange = listOf(
            pomodoroOn(LocalDate(2025, 9, 22)), // Mon
            pomodoroOn(LocalDate(2025, 9, 22)), // Mon
            pomodoroOn(LocalDate(2025, 9, 23)), // Tue
            Event(
                EventType.EXERCISE_HIGH_ENERGY_COMPLETED, emptyMap(),
                LocalDate(2025, 9, 23).atMidday(), tz.id
            ), // ignored (not a pomodoro)
        )

        val stats = getWeeklyPomodoroStatusUseCase(weekOffset = 0).first()

        assertThat(stats.weekStart.dayOfWeek).isEqualTo(DayOfWeek.SUNDAY)
        assertThat(stats.weekEnd.dayOfWeek).isEqualTo(DayOfWeek.SATURDAY)
        assertThat(stats.daily[0].count).isEqualTo(0) // Sun
        assertThat(stats.daily[1].count).isEqualTo(2) // Mon
        assertThat(stats.daily[2].count).isEqualTo(1) // Tue
        assertThat(stats.daily[3].count).isEqualTo(0) // Wed
    }

    @Test
    fun `counts pomodoros per day in previous week`() = runTest {
        fakeSystemClock.currentTime = LocalDate(2025, 9, 22).atMidday()

        fakeStatisticsEventGateway.eventsInRange = listOf(
            pomodoroOn(LocalDate(2025, 9, 14)), // Sun
            pomodoroOn(LocalDate(2025, 9, 15)), // Mon
            pomodoroOn(LocalDate(2025, 9, 17)), // Wed
        )

        val stats = getWeeklyPomodoroStatusUseCase(weekOffset = -1).first()

        assertThat(stats.weekStart).isEqualTo(LocalDate(2025, 9, 14))
        assertThat(stats.weekEnd).isEqualTo(LocalDate(2025, 9, 20))
        assertThat(stats.daily[0].count).isEqualTo(1) // Sun 14
        assertThat(stats.daily[1].count).isEqualTo(1) // Mon 15
        assertThat(stats.daily[2].count).isEqualTo(0) // Tue
        assertThat(stats.daily[3].count).isEqualTo(1) // Wed 17
        assertThat(stats.daily[4].count).isEqualTo(0) // Thu
        assertThat(stats.daily[5].count).isEqualTo(0) // Fri
        assertThat(stats.daily[6].count).isEqualTo(0) // Sat
    }
}
