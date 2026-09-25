package org.timer.main.domain.video

data class Video(
    val id: Long,
    val title: String,
    val thumbnail: String,
    val url: String,
    val category: Category
) {
    enum class Category {
        HIGH_ENERGY_SITTING, HIGH_ENERGY_STANDING, HIGH_ENERGY_PARTY_ANIMAL, HIGH_ENERGY_FLOOR_TIME,
        LOW_ENERGY_SITTING, LOW_ENERGY_STANDING, LOW_ENERGY_PARTY_ANIMAL, LOW_ENERGY_FLOOR_TIME,
        DANCE, CLEAR_YOUR_MIND
    }
}