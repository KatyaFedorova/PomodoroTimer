package org.timer.main.breakactivity

data class CurrentMenu(
    val title: String? = null,
    val menuItems: List<MenuItem>,
    val type: Type = Type.LIST
)

enum class Type {
    LIST,
    GRID,
    AUDIO
}