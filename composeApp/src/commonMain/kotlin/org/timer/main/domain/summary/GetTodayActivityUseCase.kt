package org.timer.main.domain.summary

import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import org.timer.main.domain.*
import org.timer.main.domain.project.*
import org.timer.main.domain.statistic.*

interface GetTodayActivityUseCase {
    /** Emits again whenever today's events or the projects change. */
    operator fun invoke(): Flow<DailyActivity>
}

class DefaultGetTodayActivityUseCase(
    private val statisticsEventGateway: StatisticsEventGateway,
    private val projectsGateway: ProjectsGateway,
    private val clock: SystemClock,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : GetTodayActivityUseCase {

    override fun invoke(): Flow<DailyActivity> {
        val today = Instant.fromEpochMilliseconds(clock.now()).toLocalDateTime(timeZone).date
        val fromMillis = today.atStartOfDayIn(timeZone).toEpochMilliseconds()
        val toMillis = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds()

        return combine(
            statisticsEventGateway.getEventsInRange(fromMillis, toMillis),
            projectsGateway.getProjects(),
        ) { allEvents, projects ->
            val events = allEvents.filter { it.timestamp != null && it.timestamp in fromMillis until toMillis }
            fun count(type: EventType) = events.count { it.eventType == type }

            DailyActivity(
                pomodorosCompleted = count(EventType.POMODORO_COMPLETED),
                tasksCompleted = count(EventType.TASK_COMPLETED),
                highEnergyExercises = count(EventType.EXERCISE_HIGH_ENERGY_COMPLETED),
                lowEnergyExercises = count(EventType.EXERCISE_LOW_ENERGY_COMPLETED),
                dances = count(EventType.DANCE_COMPLETED),
                clearYourMindSessions = count(EventType.CLEAR_YOUR_MIND_COMPLETED),
                projects = projects.map { project ->
                    ProjectProgress(
                        name = project.name,
                        doneTasks = project.tasks.filter { it.isDone }.map { it.description },
                        openTasks = project.tasks.count { !it.isDone },
                    )
                },
            )
        }
    }
}
