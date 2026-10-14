package org.timer.main.domain.summary

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.timer.main.domain.*
import org.timer.main.domain.project.*
import org.timer.main.domain.statistic.*
import kotlin.test.*

class DailySummaryUseCasesTest {

    private val tz = TimeZone.UTC
    private val today = LocalDateTime(2026, 10, 14, 15, 0).toInstant(tz).toEpochMilliseconds()
    private val yesterday = LocalDateTime(2026, 10, 13, 15, 0).toInstant(tz).toEpochMilliseconds()

    private val eventsGateway = FakeStatisticsEventGateway()
    private val projectsGateway = FakeProjectsGateway()
    private val clock = FakeSystemClock().apply { currentTime = today }
    private val summaryGateway = FakeSummaryGateway()

    private fun event(type: EventType, at: Long) = Event(type, emptyMap(), at, "UTC")

    @Test
    fun `today activity counts only today's events by type`() = runTest {
        eventsGateway.eventsInRange = listOf(
            event(EventType.POMODORO_COMPLETED, today),
            event(EventType.POMODORO_COMPLETED, today),
            event(EventType.POMODORO_COMPLETED, yesterday),
            event(EventType.TASK_COMPLETED, today),
            event(EventType.EXERCISE_HIGH_ENERGY_COMPLETED, today),
            event(EventType.EXERCISE_LOW_ENERGY_COMPLETED, today),
            event(EventType.DANCE_COMPLETED, today),
            event(EventType.CLEAR_YOUR_MIND_COMPLETED, today),
        )
        projectsGateway.projectsFlow.value = listOf(
            Project("p1", "Thesis", mutableListOf(Task(1, "Outline", true), Task(2, "Draft", false)))
        )

        val activity = DefaultGetTodayActivityUseCase(eventsGateway, projectsGateway, clock, tz)().first()

        assertThat(activity.pomodorosCompleted).isEqualTo(2)
        assertThat(activity.tasksCompleted).isEqualTo(1)
        assertThat(activity.breakActivities).isEqualTo(4)
        assertThat(activity.projects).isEqualTo(listOf(ProjectProgress("Thesis", listOf("Outline"), 1)))
    }

    @Test
    fun `summary prompt contains the day's numbers and project progress`() = runTest {
        val activity = DailyActivity(4, 2, 1, 0, 1, 0, listOf(ProjectProgress("Thesis", listOf("Outline"), 3)))

        DefaultGenerateDailySummaryUseCase(summaryGateway)(activity)

        val prompt = summaryGateway.lastUserPrompt!!
        assertThat(prompt).contains("- 4 focus sessions (pomodoros) completed")
        assertThat(prompt).contains("- 2 tasks completed")
        assertThat(prompt).contains("- 1 high-energy exercises during breaks")
        assertThat(prompt).contains("- 1 dances during breaks")
        assertThat(prompt)
            .contains("Not done yet today: low-energy exercises during breaks, clear-your-mind sessions during breaks")
        assertThat(prompt).contains("Thesis: done tasks: Outline; open tasks: 3")
    }

    @Test
    fun `summary prompt never lists zero counts as done`() = runTest {
        val activity = DailyActivity(0, 0, 0, 0, 2, 0, emptyList())

        DefaultGenerateDailySummaryUseCase(summaryGateway)(activity)

        val prompt = summaryGateway.lastUserPrompt!!
        assertThat(prompt).doesNotContain("- 0 ")
        assertThat(prompt).contains("- 2 dances during breaks")
    }

    @Test
    fun `summary is trimmed to five sentences`() = runTest {
        summaryGateway.response = "One. Two! Three? Four. Five. Six. Seven."
        val activity = DailyActivity(0, 0, 0, 0, 0, 0, emptyList())

        val summary = DefaultGenerateDailySummaryUseCase(summaryGateway)(activity)

        assertThat(summary).isEqualTo("One. Two! Three? Four. Five.")
    }
}
