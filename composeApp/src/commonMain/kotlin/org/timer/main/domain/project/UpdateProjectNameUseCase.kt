package org.timer.main.domain.project

class UpdateProjectNameUseCase(private val projectsGateway: ProjectsGateway) {

    suspend operator fun invoke(id: String, name: String) {
        val project = projectsGateway.getProjectById(id)
        project.name = name
        projectsGateway.updateProject(project)
    }
}