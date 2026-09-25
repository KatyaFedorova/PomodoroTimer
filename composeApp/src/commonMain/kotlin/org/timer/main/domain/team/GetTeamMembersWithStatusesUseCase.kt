package org.timer.main.domain.team

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.timer.main.domain.*
import org.timer.main.domain.exercise.*
import org.timer.main.domain.timer.*

interface GetTeamMembersWithStatusesUseCase {
    operator fun invoke(): Flow<List<TeamMember>>
}

class DefaultGetTeamMembersStatusesUseCase(
    private val teamGateway: TeamGateway,
    private val timerGateway: TimerGateway,
    private val exerciseGateway: ExerciseGateway,
    private val clock: SystemClock
) : GetTeamMembersWithStatusesUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(): Flow<List<TeamMember>> {
        return teamGateway.getMyTeamId()
            .flatMapLatest { teamId ->
                if (teamId.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    teamGateway.getMembersByTeamId(teamId)
                        .flatMapLatest { users ->
                            if (users.isEmpty()) {
                                flowOf(emptyList())
                            } else {
                                val ticker = tickerFlow(periodMillis = 1000)

                                combine(
                                    users.map { user ->
                                        combine(
                                            timerGateway.getTimerByUserId(user.id),
                                            exerciseGateway.getProgressByUserId(user.id),
                                            ticker
                                        ) { timer, exerciseProgress, _ ->
                                            TeamMember(
                                                userId = user.id,
                                                email = user.email,
                                                name = user.name,
                                                isPomodoroRunning = timer.isPomodoroRunning(clock.now()),
                                                isShortBreakRunning = timer.isShortBreakRunning(clock.now()),
                                                isLongBreakRunning = timer.isLongBreakRunning(clock.now()),
                                                kittidoros = timer.getTodayKittidoros(clock),
                                                exerciseCount = exerciseProgress.todayCount(clock)
                                            )
                                        }
                                    }
                                ) { it.toList() }
                            }
                        }
                }
            }
            .distinctUntilChanged()
    }

    private fun tickerFlow(periodMillis: Long): Flow<Long> = flow {
        while (true) {
            emit(clock.now())
            delay(periodMillis)
        }
    }
}

data class TeamMember(
    val userId: String,
    val email: String,
    val name: String?,
    val isPomodoroRunning: Boolean,
    val isShortBreakRunning: Boolean,
    val isLongBreakRunning: Boolean,
    val kittidoros: Int,
    val exerciseCount: Int
)
