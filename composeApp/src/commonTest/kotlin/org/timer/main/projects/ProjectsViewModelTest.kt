package org.timer.main.projects

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.project.*
import org.timer.main.domain.statistic.*
import org.timer.main.projects.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class ProjectsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var projectsGateway: FakeProjectsGateway
    private lateinit var viewModel: ProjectsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        projectsGateway = FakeProjectsGateway()
        val saveEventUseCase = FakeSaveEventUseCase()
        viewModel = ProjectsViewModel(
            GetProjectsUseCase(projectsGateway),
            AddProjectUseCase(projectsGateway),
            RemoveProjectUseCase(projectsGateway),
            AddTaskUseCase(projectsGateway),
            DoneTaskUseCase(projectsGateway, saveEventUseCase),
            UndoneTaskUseCase(projectsGateway),
            UpdateProjectsOrderUseCase(projectsGateway),
            UpdateTasksOrderUseCase(projectsGateway),
            UpdateProjectNameUseCase(projectsGateway),
            UpdateTaskDescriptionUseCase(projectsGateway),
            DeleteTaskUseCase(projectsGateway),
            MoveTaskToTheEndOfListUseCase(projectsGateway),
            DeleteAllDoneTasksUseCase(projectsGateway),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should show no projects`() = runTest {
        assertThat(viewModel.viewState.first().projects.size).isEqualTo(0)
    }

    @Test
    fun `projects from the gateway should be shown on screen`() = runTest {
        projectsGateway.projectsFlow.value = listOf(
            Project("1", "Home", mutableListOf()),
            Project("2", "Work", mutableListOf()),
        )
        advanceUntilIdle()
        val names = viewModel.viewState.value.projects.map { it.name }
        assertThat(names).isEqualTo(listOf("Home", "Work"))
    }

    @Test
    fun `typing a project title should update the draft name and enable submit`() = runTest {
        viewModel.onProjectTitleUpdate("Groceries")
        val state = viewModel.viewState.value
        assertThat(state.projectName).isEqualTo("Groceries")
        assertThat(state.isSubmitEnabled).isTrue()
    }

    @Test
    fun `submitting a new project should add it to the shown projects`() = runTest {
        viewModel.onProjectTitleUpdate("Groceries")
        viewModel.onAddProjectSubmitClick()
        advanceUntilIdle()
        val names = viewModel.viewState.value.projects.map { it.name }
        assertThat(names).isEqualTo(listOf("Groceries"))
    }

    @Test
    fun `removing a project should take it off the screen`() = runTest {
        projectsGateway.projectsFlow.value = listOf(
            Project("1", "Home", mutableListOf()),
            Project("2", "Work", mutableListOf()),
        )
        advanceUntilIdle()
        viewModel.removeProject("1")
        advanceUntilIdle()
        val names = viewModel.viewState.value.projects.map { it.name }
        assertThat(names).isEqualTo(listOf("Work"))
    }
}
