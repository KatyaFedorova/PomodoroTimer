package org.timer.main.domain.project

import kotlinx.coroutines.flow.*

expect class FirebaseProjectsGateway() : ProjectsGateway {
    override fun getProjects(): Flow<List<Project>>

    override suspend fun getProjectById(projectId: String): Project

    override suspend fun saveProject(project: Project)

    override suspend fun updateProject(project: Project)

    override suspend fun updateProjects(projects: List<Project>)

    override suspend fun removeProjectById(id: String)
}