package org.timer.main.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.timer.main.domain.statistic.*

class PlotViewModel(
    private val getWeeklyPomodoroStatsUseCase: GetWeeklyPomodoroStatsUseCase,
    private val getMonthlyPomodoroStatsUseCase: GetMonthlyPomodoroStatsUseCase,
    private val getWeeklyExerciseStatsUseCase: GetWeeklyExerciseStatsUseCase,
    private val getMonthlyExerciseStatsUseCase: GetMonthlyExerciseStatsUseCase,
    private val getWeeklyTaskStatsUseCase: GetWeeklyTaskStatsUseCase,
    private val getMonthlyTaskStatsUseCase: GetMonthlyTaskStatsUseCase
) : ViewModel() {

    private val _viewState = MutableStateFlow(PlotViewState())
    val viewState: StateFlow<PlotViewState> = _viewState

    private val jobs = mutableMapOf<String, Job>()

    init {
        loadPomodoroWeek(0)
        loadPomodoroMonth(0)
        loadTaskWeek(0)
        loadTaskMonth(0)
        loadExerciseWeek(0)
        loadExerciseMonth(0)
    }

    // ────────────────────────────── Pomodoro ──────────────────────────────
    fun onPrevPomodoroWeek() = loadPomodoroWeek(_viewState.value.pomodoroWeekOffset - 1)
    fun onNextPomodoroWeek() = loadPomodoroWeek(_viewState.value.pomodoroWeekOffset + 1)

    fun onPrevPomodoroMonth() = loadPomodoroMonth(_viewState.value.pomodoroMonthOffset - 1)
    fun onNextPomodoroMonth() = loadPomodoroMonth(_viewState.value.pomodoroMonthOffset + 1)

    private fun loadPomodoroWeek(offset: Int) {
        jobs["pomodoro_week"]?.cancel()
        jobs["pomodoro_week"] = viewModelScope.launch {
            getWeeklyPomodoroStatsUseCase(offset).collect { stats ->
                _viewState.update { it.copy(
                    pomodoroWeekOffset = offset,
                    pomodoroWeekly = stats.daily,
                    pomodoroWeekStart = stats.weekStart,
                    pomodoroWeekEnd = stats.weekEnd
                ) }
            }
        }
    }

    private fun loadPomodoroMonth(offset: Int) {
        jobs["pomodoro_month"]?.cancel()
        jobs["pomodoro_month"] = viewModelScope.launch {
            getMonthlyPomodoroStatsUseCase(offset).collect { stats ->
                _viewState.update { it.copy(
                    pomodoroMonthOffset = offset,
                    pomodoroMonthly = stats.daily,
                    pomodoroMonthStart = stats.monthStart,
                    pomodoroMonthEnd = stats.monthEnd
                ) }
            }
        }
    }

    // ────────────────────────────── Task ──────────────────────────────
    fun onPrevTaskWeek() = loadTaskWeek(_viewState.value.taskWeekOffset - 1)
    fun onNextTaskWeek() = loadTaskWeek(_viewState.value.taskWeekOffset + 1)

    fun onPrevTaskMonth() = loadTaskMonth(_viewState.value.taskMonthOffset - 1)
    fun onNextTaskMonth() = loadTaskMonth(_viewState.value.taskMonthOffset + 1)

    private fun loadTaskWeek(offset: Int) {
        jobs["task_week"]?.cancel()
        jobs["task_week"] = viewModelScope.launch {
            getWeeklyTaskStatsUseCase(offset).collect { stats ->
                _viewState.update { it.copy(
                    taskWeekOffset = offset,
                    taskWeekly = stats.daily,
                    taskWeekStart = stats.weekStart,
                    taskWeekEnd = stats.weekEnd
                ) }
            }
        }
    }

    private fun loadTaskMonth(offset: Int) {
        jobs["task_month"]?.cancel()
        jobs["task_month"] = viewModelScope.launch {
            getMonthlyTaskStatsUseCase(offset).collect { stats ->
                _viewState.update { it.copy(
                    taskMonthOffset = offset,
                    taskMonthly = stats.daily,
                    taskMonthStart = stats.monthStart,
                    taskMonthEnd = stats.monthEnd
                ) }
            }
        }
    }

    // ────────────────────────────── Exercise ──────────────────────────────
    fun onPrevExerciseWeek() = loadExerciseWeek(_viewState.value.exerciseWeekOffset - 1)
    fun onNextExerciseWeek() = loadExerciseWeek(_viewState.value.exerciseWeekOffset + 1)

    fun onPrevExerciseMonth() = loadExerciseMonth(_viewState.value.exerciseMonthOffset - 1)
    fun onNextExerciseMonth() = loadExerciseMonth(_viewState.value.exerciseMonthOffset + 1)

    private fun loadExerciseWeek(offset: Int) {
        jobs["exercise_week"]?.cancel()
        jobs["exercise_week"] = viewModelScope.launch {
            getWeeklyExerciseStatsUseCase(offset).collect { stats ->
                _viewState.update { it.copy(
                    exerciseWeekOffset = offset,
                    exerciseWeekly = stats.daily,
                    exerciseWeekStart = stats.weekStart,
                    exerciseWeekEnd = stats.weekEnd
                ) }
            }
        }
    }

    private fun loadExerciseMonth(offset: Int) {
        jobs["exercise_month"]?.cancel()
        jobs["exercise_month"] = viewModelScope.launch {
            getMonthlyExerciseStatsUseCase(offset).collect { stats ->
                _viewState.update { it.copy(
                    exerciseMonthOffset = offset,
                    exerciseMonthly = stats.daily,
                    exerciseMonthStart = stats.monthStart,
                    exerciseMonthEnd = stats.monthEnd
                ) }
            }
        }
    }
}
