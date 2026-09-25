package org.timer.main.domain.project

class DeleteTaskUseCase(private val projectsGateway: ProjectsGateway) {

    suspend operator fun invoke(projectId: String, taskId: Long) {
        val project = projectsGateway.getProjectById(projectId)
        project.tasks.removeAt(project.tasks.indexOfFirst { it.id == taskId })
        projectsGateway.updateProject(project)
    }
}