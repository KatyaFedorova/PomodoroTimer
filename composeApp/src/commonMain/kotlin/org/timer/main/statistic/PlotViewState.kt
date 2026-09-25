package org.timer.main.statistic

import kotlinx.datetime.LocalDate
import org.timer.main.domain.statistic.DailyEventStat

data class PlotViewState(
    // ────────────────────────────── Pomodoro ──────────────────────────────
    val pomodoroWeekOffset: Int = 0,
    val pomodoroWeekly: List<DailyEventStat> = emptyList(),
    val pomodoroWeekStart: LocalDate? = null,
    val pomodoroWeekEnd: LocalDate? = null,

    val pomodoroMonthOffset: Int = 0,
    val pomodoroMonthly: List<DailyEventStat> = emptyList(),
    val pomodoroMonthStart: LocalDate? = null,
    val pomodoroMonthEnd: LocalDate? = null,
    // ────────────────────────────── Task ──────────────────────────────
    val taskWeekOffset: Int = 0,
    val taskWeekly: List<DailyEventStat> = emptyList(),
    val taskWeekStart: LocalDate? = null,
    val taskWeekEnd: LocalDate? = null,

    val taskMonthOffset: Int = 0,
    val taskMonthly: List<DailyEventStat> = emptyList(),
    val taskMonthStart: LocalDate? = null,
    val taskMonthEnd: LocalDate? = null,
    // ────────────────────────────── Exercise ──────────────────────────────
    val exerciseWeekOffset: Int = 0,
    val exerciseWeekly: List<DailyEventStat> = emptyList(),
    val exerciseWeekStart: LocalDate? = null,
    val exerciseWeekEnd: LocalDate? = null,

    val exerciseMonthOffset: Int = 0,
    val exerciseMonthly: List<DailyEventStat> = emptyList(),
    val exerciseMonthStart: LocalDate? = null,
    val exerciseMonthEnd: LocalDate? = null
) {

    fun weekLabel(offset: Int, start: LocalDate?, end: LocalDate?): String = when (offset) {
        0 -> "This week"
        -1 -> "Last week"
        1 -> "Next week"
        else -> if (start != null && end != null) "$start – $end" else ""
    }

    fun monthLabel(offset: Int, start: LocalDate?, end: LocalDate?): String = when (offset) {
        0 -> "This month"
        -1 -> "Last month"
        1 -> "Next month"
        else -> if (start != null && end != null) "$start – $end" else ""
    }
}
