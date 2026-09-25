package org.timer.main.statistic

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import kotlinx.datetime.*
import org.koin.compose.viewmodel.*
import org.timer.main.*
import org.timer.main.domain.statistic.*
import org.timer.main.progress.header.*
import kotlin.math.*

enum class ChartMode { WEEKLY, MONTHLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotContent(viewModel: PlotViewModel = koinViewModel()) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    val charts = listOf(
        // 🕑 Pomodoro
        ChartConfig(
            title = "Pomodoros",
            weekly = { viewState.pomodoroWeekly },
            monthly = { viewState.pomodoroMonthly },
            weekLabel = {
                viewState.weekLabel(
                    viewState.pomodoroWeekOffset,
                    viewState.pomodoroWeekStart,
                    viewState.pomodoroWeekEnd
                )
            },
            monthLabel = {
                viewState.monthLabel(
                    viewState.pomodoroMonthOffset,
                    viewState.pomodoroMonthStart,
                    viewState.pomodoroMonthEnd
                )
            },
            onPrevWeek = { viewModel.onPrevPomodoroWeek() },
            onNextWeek = { viewModel.onNextPomodoroWeek() },
            onPrevMonth = { viewModel.onPrevPomodoroMonth() },
            onNextMonth = { viewModel.onNextPomodoroMonth() }
        ),

        // ✅ Tasks
        ChartConfig(
            title = "Tasks",
            weekly = { viewState.taskWeekly },
            monthly = { viewState.taskMonthly },
            weekLabel = {
                viewState.weekLabel(
                    viewState.taskWeekOffset,
                    viewState.taskWeekStart,
                    viewState.taskWeekEnd
                )
            },
            monthLabel = {
                viewState.monthLabel(
                    viewState.taskMonthOffset,
                    viewState.taskMonthStart,
                    viewState.taskMonthEnd
                )
            },
            onPrevWeek = { viewModel.onPrevTaskWeek() },
            onNextWeek = { viewModel.onNextTaskWeek() },
            onPrevMonth = { viewModel.onPrevTaskMonth() },
            onNextMonth = { viewModel.onNextTaskMonth() }
        ),

        // 🏋️ Exercise
        ChartConfig(
            title = "Exercises",
            weekly = { viewState.exerciseWeekly },
            monthly = { viewState.exerciseMonthly },
            weekLabel = {
                viewState.weekLabel(
                    viewState.exerciseWeekOffset,
                    viewState.exerciseWeekStart,
                    viewState.exerciseWeekEnd
                )
            },
            monthLabel = {
                viewState.monthLabel(
                    viewState.exerciseMonthOffset,
                    viewState.exerciseMonthStart,
                    viewState.exerciseMonthEnd
                )
            },
            onPrevWeek = { viewModel.onPrevExerciseWeek() },
            onNextWeek = { viewModel.onNextExerciseWeek() },
            onPrevMonth = { viewModel.onPrevExerciseMonth() },
            onNextMonth = { viewModel.onNextExerciseMonth() }
        )
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val fixedColor = MaterialTheme.colorScheme.surface
    val windowInfo = rememberWindowInfo()
    Column {
        if (windowInfo.isSmallScreen()) {
            CatBowlProgressHeader()
        }
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = fixedColor,
                        scrolledContainerColor = fixedColor
                    )
                )
            }
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(charts) { chart ->
                    ChartCard(chart)
                }
            }
        }
    }
}


data class ChartConfig(
    val title: String,
    val weekly: () -> List<DailyEventStat>,
    val monthly: () -> List<DailyEventStat>,
    val weekLabel: () -> String,
    val monthLabel: () -> String,
    val onPrevWeek: () -> Unit,
    val onNextWeek: () -> Unit,
    val onPrevMonth: () -> Unit,
    val onNextMonth: () -> Unit
)

@Composable
private fun ChartCard(chart: ChartConfig) {
    var chartMode by remember { mutableStateOf(ChartMode.WEEKLY) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = chart.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = chartMode == ChartMode.WEEKLY,
                    onClick = { chartMode = ChartMode.WEEKLY },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("Week") }

                SegmentedButton(
                    selected = chartMode == ChartMode.MONTHLY,
                    onClick = { chartMode = ChartMode.MONTHLY },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("Month") }
            }

            Spacer(Modifier.height(16.dp))

            Crossfade(chartMode) { mode ->
                when (mode) {
                    ChartMode.WEEKLY -> WeeklyLineChart(
                        title = chart.weekLabel(),
                        data = chart.weekly(),
                        onPrevWeek = chart.onPrevWeek,
                        onNextWeek = chart.onNextWeek
                    )

                    ChartMode.MONTHLY -> MonthlyLineChart(
                        title = chart.monthLabel(),
                        data = chart.monthly(),
                        onPrevMonth = chart.onPrevMonth,
                        onNextMonth = chart.onNextMonth
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyLineChart(
    title: String,
    data: List<DailyEventStat>,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val dailyByDate = data.associateBy { it.date }
    val orderedDates: List<LocalDate> = data.map { it.date }.sorted()
    val values = orderedDates.map { d -> dailyByDate[d]?.count ?: 0 }
    val labelsX = orderedDates.map { d ->
        d.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.titlecase() }
    }

    ChartContainer(
        title = title,
        values = values,
        dates = orderedDates,
        labelsX = labelsX,
        onPrev = onPrevWeek,
        onNext = onNextWeek
    )
}

@Composable
fun MonthlyLineChart(
    title: String,
    data: List<DailyEventStat>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthDates = data.map { it.date }.sorted()
    val dailyByDate = data.associateBy { it.date }
    val values = monthDates.map { d -> dailyByDate[d]?.count ?: 0 }

    val tickCountX = 7
    val tickIndices = if (monthDates.isNotEmpty()) {
        val denom = (tickCountX - 1).coerceAtLeast(1)
        (0 until tickCountX).map { i ->
            ((monthDates.lastIndex) * (i / denom.toFloat()))
                .toInt().coerceIn(0, monthDates.lastIndex)
        }.distinct().sorted()
    } else emptyList()
    val tickLabelsX = tickIndices.map { idx -> monthDates[idx].dayOfMonth.toString() }

    ChartContainer(
        title = title,
        values = values,
        dates = monthDates,
        labelsX = tickLabelsX,
        onPrev = onPrevMonth,
        onNext = onNextMonth
    )
}


@Composable
private fun ChartContainer(
    title: String,
    values: List<Int>,
    dates: List<LocalDate>,
    labelsX: List<String>,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val strokeWidthPx = with(LocalDensity.current) { 2.dp.toPx() }
    val pointRadiusPx = with(LocalDensity.current) { 4.dp.toPx() }
    val gridStrokePx = with(LocalDensity.current) { 1.dp.toPx() }

    val chartHeight = 300.dp
    val yAxisWidth = 24.dp
    val yAxisSpacing = 8.dp
    val innerPad = 16.dp

    val desiredTicks = 4
    val maxVal: Int = max(values.maxOrNull() ?: 0, 1)
    val step: Int = max(1, ceil(maxVal / desiredTicks.toFloat()).toInt())
    val roundedMax: Int = step * desiredTicks
    val yTicks: List<Int> = (0..desiredTicks).map { it * step }
    val yTicksTopToBottom = yTicks.reversed()

    Column(Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Arrow Back")
            }
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Arrow Forward")
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            Column(
                modifier = Modifier
                    .width(yAxisWidth)
                    .fillMaxHeight()
                    .padding(vertical = innerPad),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yTicksTopToBottom.forEach { v ->
                    Text(
                        text = v.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(yAxisSpacing))

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color(0xFFF5F5F5))
                    .padding(innerPad)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val n = values.size
                    if (n == 0) return@Canvas

                    val roundedMaxF = roundedMax.toFloat()
                    val stepX = if (n > 1) size.width / (n - 1).toFloat() else 0f
                    val stepY = if (roundedMaxF > 0f) size.height / roundedMaxF else 0f

                    for (i in 0..desiredTicks) {
                        val y: Float = size.height - (yTicks[i].toFloat() * stepY)
                        drawLine(
                            color = Color.Black.copy(alpha = 0.06f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = gridStrokePx
                        )
                    }

                    val validPoints = values.mapIndexedNotNull { i, v ->
                        val x = stepX * i.toFloat()
                        val y = size.height - (v.toFloat() * stepY)
                        val date = dates[i]
                        if (v == 0 && date >= today) null else Offset(x, y)
                    }

                    for (i in 0 until validPoints.size - 1) {
                        drawLine(
                            color = Color(0xFF2979FF),
                            start = validPoints[i],
                            end = validPoints[i + 1],
                            strokeWidth = strokeWidthPx
                        )
                    }

                    validPoints.forEach { p ->
                        drawCircle(
                            color = Color(0xFF2979FF),
                            radius = pointRadiusPx,
                            center = p
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = yAxisWidth + yAxisSpacing)
        ) {
            labelsX.forEach { day ->
                Text(day, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


