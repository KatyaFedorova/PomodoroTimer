package org.timer.main.breakactivity

data class MenuItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl:String?=null,
    val children: CurrentMenu?=null
)