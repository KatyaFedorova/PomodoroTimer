package org.timer.main.domain.project

import kotlinx.serialization.*

@Serializable
data class Project(
    val id: String,
    var name: String,
    val tasks: MutableList<Task>,
    val order: Int = 0, //todo move to gateway
    )

fun List<Project>.deepCopy(): List<Project> {
    val newList = mutableListOf<Project>()
    this.forEach {
        newList.add(Project(it.id, it.name,  addTasks(it.tasks)))
    }
    return newList
}

private fun addTasks(tasks: MutableList<Task>): MutableList<Task> {
    val newList = mutableListOf<Task>()
    tasks.forEach {
        newList.add(Task(it.id, it.description, it.isDone))
    }
    return newList
}