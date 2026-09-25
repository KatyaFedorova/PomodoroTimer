package org.timer.main.domain.project

import kotlinx.coroutines.flow.*

interface ProjectsGateway {
    fun getProjects(): Flow<List<Project>>

    suspend fun getProjectById(projectId: String): Project

    suspend fun saveProject(project: Project)

    suspend fun updateProject(project: Project)

    suspend fun updateProjects(projects: List<Project>)

    suspend fun removeProjectById(id: String)

}