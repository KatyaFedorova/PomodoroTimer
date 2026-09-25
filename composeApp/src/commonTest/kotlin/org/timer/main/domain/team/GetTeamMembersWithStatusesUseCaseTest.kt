package org.timer.main.domain.team

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.*
import org.timer.main.domain.game.*
import org.timer.main.domain.exercise.*
import org.timer.main.domain.timer.*
import kotlin.test.*

class GetTeamMembersWithStatusesUseCaseTest {

    private lateinit var teamGateway: FakeTeamGateway
    private lateinit var timerGateway: FakeTimerGateway
    private lateinit var exerciseGateway: FakeExerciseGateway
    private lateinit var useCase: GetTeamMembersWithStatusesUseCase

    @BeforeTest
    fun setUp() {
        teamGateway = FakeTeamGateway()
        timerGateway = FakeTimerGateway()
        exerciseGateway = FakeExerciseGateway()
        useCase = DefaultGetTeamMembersStatusesUseCase(
            teamGateway, timerGateway, exerciseGateway, FakeSystemClock()
        )
    }

    @Test
    fun `belonging to no team yields no members`() = runTest {
        teamGateway.myTeamId.value = null

        assertThat(useCase.invoke().first()).isEqualTo(emptyList<TeamMember>())
    }

    @Test
    fun `an empty team yields no members`() = runTest {
        teamGateway.myTeamId.value = "team1"
        teamGateway.membersByTeamId.value = emptyList()

        assertThat(useCase.invoke().first()).isEqualTo(emptyList<TeamMember>())
    }

    @Test
    fun `an idle team member is mapped with no running timer`() = runTest {
        teamGateway.myTeamId.value = "team1"
        teamGateway.membersByTeamId.value = listOf(User("u1", "u1@paws.com", "Adam"))

        val members = useCase.invoke().first()

        assertThat(members.size).isEqualTo(1)
        assertThat(members.first().name).isEqualTo("Adam")
        assertThat(members.first().isPomodoroRunning).isFalse()
    }
}
