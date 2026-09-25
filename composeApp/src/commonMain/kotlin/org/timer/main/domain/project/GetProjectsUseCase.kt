package org.timer.main.domain.project

class GetProjectsUseCase(private val projectsGateway: ProjectsGateway) {
    operator fun invoke() = projectsGateway.getProjects()
}