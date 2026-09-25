package org.timer.main.team.activity

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.team.*
import org.timer.main.team.activity.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class TeamActivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getTeamMembersUseCase: FakeGetTeamMembersWithStatusesUseCase
    private lateinit var viewModel: TeamActivityViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getTeamMembersUseCase = FakeGetTeamMembersWithStatusesUseCase()
        viewModel = TeamActivityViewModel(getTeamMembersUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should show no team members`() = runTest {
        assertThat(viewModel.viewState.first().members.size).isEqualTo(0)
    }

    @Test
    fun `team members should be shown sorted by name`() = runTest {
        getTeamMembersUseCase.membersFlow.value = listOf(
            teamMember(userId = "1", name = "Zoe"),
            teamMember(userId = "2", name = "Adam"),
        )
        advanceUntilIdle()
        val names = viewModel.viewState.value.members.map { it.name }
        assertThat(names).isEqualTo(listOf("Adam", "Zoe"))
    }

    @Test
    fun `member without a name should be shown by email`() = runTest {
        getTeamMembersUseCase.membersFlow.value =
            listOf(teamMember(userId = "1", name = null, email = "cat@paws.com"))
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.members.first().name).isEqualTo("cat@paws.com")
    }

    @Test
    fun `member doing nothing should be shown as available`() = runTest {
        getTeamMembersUseCase.membersFlow.value = listOf(teamMember(userId = "1", name = "Adam"))
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.members.first().aviable).isTrue()
    }

    @Test
    fun `member running a pomodoro should not be shown as available`() = runTest {
        getTeamMembersUseCase.membersFlow.value =
            listOf(teamMember(userId = "1", name = "Adam", isPomodoroRunning = true))
        advanceUntilIdle()
        val member = viewModel.viewState.value.members.first()
        assertThat(member.onPomodoro).isTrue()
        assertThat(member.aviable).isFalse()
    }

    private fun teamMember(
        userId: String,
        name: String?,
        email: String = "user@paws.com",
        isPomodoroRunning: Boolean = false,
        isShortBreakRunning: Boolean = false,
        isLongBreakRunning: Boolean = false,
        kittidoros: Int = 0,
        exerciseCount: Int = 0,
    ) = TeamMember(
        userId = userId,
        email = email,
        name = name,
        isPomodoroRunning = isPomodoroRunning,
        isShortBreakRunning = isShortBreakRunning,
        isLongBreakRunning = isLongBreakRunning,
        kittidoros = kittidoros,
        exerciseCount = exerciseCount,
    )
}
