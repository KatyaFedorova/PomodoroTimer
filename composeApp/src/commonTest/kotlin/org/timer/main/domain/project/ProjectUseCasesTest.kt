package org.timer.main.domain.project

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.statistic.*
import kotlin.test.*

class ProjectUseCasesTest {

    private lateinit var gateway: FakeProjectsGateway

    @BeforeTest
    fun setUp() {
        gateway = FakeProjectsGateway()
    }

    private fun task(id: Long, description: String = "task $id", done: Boolean = false) =
        Task(id, description, done)

    private fun project(
        id: String = "p1",
        name: String = "Home",
        tasks: MutableList<Task> = mutableListOf(),
    ) = Project(id, name, tasks)

    private fun projectById(id: String) = gateway.projectsFlow.value.first { it.id == id }

    @Test
    fun `adding a project stores it in the gateway`() = runTest {
        AddProjectUseCase(gateway).invoke("Work")

        val projects = gateway.getProjects().first()
        assertThat(projects.map { it.name }).isEqualTo(listOf("Work"))
    }

    @Test
    fun `getting projects returns what the gateway holds`() = runTest {
        gateway.projectsFlow.value = listOf(project(id = "1", name = "Home"), project(id = "2", name = "Work"))

        val names = GetProjectsUseCase(gateway).invoke().first().map { it.name }
        assertThat(names).isEqualTo(listOf("Home", "Work"))
    }

    @Test
    fun `removing a project deletes it from the gateway`() = runTest {
        gateway.projectsFlow.value = listOf(project(id = "1"), project(id = "2", name = "Work"))

        RemoveProjectUseCase(gateway).invoke("1")

        assertThat(gateway.getProjects().first().map { it.id }).isEqualTo(listOf("2"))
    }

    @Test
    fun `updating a project name changes it`() = runTest {
        gateway.projectsFlow.value = listOf(project(id = "1", name = "Home"))

        UpdateProjectNameUseCase(gateway).invoke("1", "Renamed")

        assertThat(projectById("1").name).isEqualTo("Renamed")
    }

    @Test
    fun `adding a task appends it to the project`() = runTest {
        gateway.projectsFlow.value = listOf(project(id = "1", tasks = mutableListOf()))

        AddTaskUseCase(gateway).invoke("1", "Buy milk")

        val tasks = projectById("1").tasks
        assertThat(tasks.size).isEqualTo(1)
        assertThat(tasks.first().description).isEqualTo("Buy milk")
        assertThat(tasks.first().isDone).isFalse()
    }

    @Test
    fun `adding a task inserts it before the first done task`() = runTest {
        gateway.projectsFlow.value = listOf(
            project(id = "1", tasks = mutableListOf(task(1, done = false), task(2, done = true)))
        )

        AddTaskUseCase(gateway).invoke("1", "New")

        val descriptions = projectById("1").tasks.map { it.description }
        assertThat(descriptions).isEqualTo(listOf("task 1", "New", "task 2"))
    }

    @Test
    fun `deleting a task removes it from the project`() = runTest {
        gateway.projectsFlow.value =
            listOf(project(id = "1", tasks = mutableListOf(task(1), task(2))))

        DeleteTaskUseCase(gateway).invoke("1", 1)

        assertThat(projectById("1").tasks.map { it.id }).isEqualTo(listOf(2L))
    }

    @Test
    fun `marking a task done sets its done flag`() = runTest {
        gateway.projectsFlow.value =
            listOf(project(id = "1", tasks = mutableListOf(task(1, done = false))))

        DoneTaskUseCase(gateway, FakeSaveEventUseCase()).invoke("1", 1)

        assertThat(projectById("1").tasks.first { it.id == 1L }.isDone).isTrue()
    }

    @Test
    fun `marking a task undone clears its done flag`() = runTest {
        gateway.projectsFlow.value =
            listOf(project(id = "1", tasks = mutableListOf(task(1, done = true))))

        UndoneTaskUseCase(gateway).invoke("1", 1)

        assertThat(projectById("1").tasks.first { it.id == 1L }.isDone).isFalse()
    }

    @Test
    fun `updating a task description changes it`() = runTest {
        gateway.projectsFlow.value =
            listOf(project(id = "1", tasks = mutableListOf(task(1, description = "old"))))

        UpdateTaskDescriptionUseCase(gateway).invoke("1", 1, "new")

        assertThat(projectById("1").tasks.first { it.id == 1L }.description).isEqualTo("new")
    }

    @Test
    fun `moving a task to the end reorders it last`() = runTest {
        gateway.projectsFlow.value =
            listOf(project(id = "1", tasks = mutableListOf(task(1), task(2), task(3))))

        MoveTaskToTheEndOfListUseCase(gateway).invoke("1", 1)

        assertThat(projectById("1").tasks.map { it.id }).isEqualTo(listOf(2L, 3L, 1L))
    }

    @Test
    fun `reordering projects moves a project to the new position`() = runTest {
        gateway.projectsFlow.value =
            listOf(project(id = "1"), project(id = "2"), project(id = "3"))

        UpdateProjectsOrderUseCase(gateway).invoke(fromIndex = 0, toIndex = 2)

        assertThat(gateway.getProjects().first().map { it.id }).isEqualTo(listOf("2", "3", "1"))
    }

    @Test
    fun `reordering tasks moves a task to the new position`() = runTest {
        gateway.projectsFlow.value =
            listOf(project(id = "1", tasks = mutableListOf(task(1), task(2), task(3))))

        UpdateTasksOrderUseCase(gateway).invoke("1", fromIndex = 0, toIndex = 2)

        assertThat(projectById("1").tasks.map { it.id }).isEqualTo(listOf(2L, 3L, 1L))
    }

    @Test
    fun `deleting all done tasks keeps only the unfinished ones`() = runTest {
        gateway.projectsFlow.value = listOf(
            project(
                id = "1",
                tasks = mutableListOf(task(1, done = true), task(2, done = false), task(3, done = true))
            )
        )

        DeleteAllDoneTasksUseCase(gateway).invoke("1")

        assertThat(projectById("1").tasks.map { it.id }).isEqualTo(listOf(2L))
    }
}
