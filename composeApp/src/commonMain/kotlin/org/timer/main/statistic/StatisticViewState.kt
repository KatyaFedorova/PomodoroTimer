package org.timer.main.statistic

import org.jetbrains.compose.resources.*
import pomodorotimer.composeapp.generated.resources.*

data class StatisticViewState(
    val feedingDates: List<Long> = emptyList(),
    val showFishNumberText: Boolean = false,
    val totalFishes: Int = 0,
    val fishImageRes: DrawableResource = Res.drawable.fishes0,
)

