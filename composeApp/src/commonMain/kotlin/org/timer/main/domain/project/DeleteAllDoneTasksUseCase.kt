package org.timer.main.domain.project

class DeleteAllDoneTasksUseCase(private val projectsGateway: ProjectsGateway) {

    suspend operator fun invoke(projectId: String) {
        val project = projectsGateway.getProjectById(projectId)
        project.tasks.removeAll { it.isDone }
        projectsGateway.updateProject(project)
    }
}