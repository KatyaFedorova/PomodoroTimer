import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import kotlinx.datetime.*
import org.jetbrains.compose.resources.*
import org.timer.main.*
import pomodorotimer.composeapp.generated.resources.*

@Composable
fun Calendar(
    highlightedDates: List<Long>,
    modifier: Modifier = Modifier
) {
    var currentYear by remember { mutableStateOf(currentDate().year) }
    var currentMonth by remember { mutableStateOf(currentDate().monthNumber) }
    val highlightedLocalDates = remember(highlightedDates) {
        highlightedDates.map {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        }.toSet()
    }

    val today = currentDate()
    val firstDayOfMonth = LocalDate(currentYear, currentMonth, 1)
    val daysInMonth = daysInMonth(currentYear, currentMonth)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber % 7
    val totalCells = firstDayOfWeek + daysInMonth
    val weeksCount = (totalCells + 6) / 7

    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        BoxWithConstraints(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp)) {
            val cellWidth = maxWidth / 7
            val cellHeight = maxWidth / 14

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous month",
                        modifier = Modifier
                            .clickable {
                                if (currentMonth == 1) {
                                    currentMonth = 12
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            }
                            .padding(8.dp)
                    )

                    Text(
                        text = "${firstDayOfMonth.month.name.take(3)} ${firstDayOfMonth.year}",
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next month",
                        modifier = Modifier
                            .clickable {
                                if (currentMonth == 12) {
                                    currentMonth = 1
                                    currentYear += 1
                                } else {
                                    currentMonth += 1
                                }
                            }
                            .padding(8.dp)
                    )
                }

                Row {
                    listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .height(cellHeight * 0.8f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = day, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                var dayCounter = 1
                repeat(weeksCount) { week ->
                    Row {
                        repeat(7) { dayIndex ->
                            val cellIndex = week * 7 + dayIndex
                            if (cellIndex >= firstDayOfWeek && dayCounter <= daysInMonth) {
                                val currentDate = LocalDate(currentYear, currentMonth, dayCounter)
                                val isHighlighted = currentDate in highlightedLocalDates
                                val isToday = currentDate == today
                                DayCell(
                                    day = dayCounter,
                                    isHighlighted = isHighlighted,
                                    isToday = isToday,
                                    modifier = Modifier
                                        .width(cellWidth)
                                        .height(cellHeight)
                                )
                                dayCounter++
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .width(cellWidth)
                                        .height(cellHeight)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(day: Int, isHighlighted: Boolean, isToday: Boolean, modifier: Modifier = Modifier) {
    val textModifier = Modifier.offset(y = (-1).dp)
    val defaultTextColor = MaterialTheme.colorScheme.onBackground
    val highlightTextColor = MaterialTheme.colorScheme.onPrimary
    val todayBorderColor = MaterialTheme.colorScheme.onBackground


    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            isToday && isHighlighted -> {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.fish),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.1f)
                            .offset(x = 1.dp, y = 1.dp)
                            .graphicsLayer { clip = false },
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(1.dp, todayBorderColor)
                    )
                    Text(
                        text = day.toString(),
                        color = highlightTextColor,
                        modifier = textModifier
                    )
                }
            }

            isHighlighted -> {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.fish),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.1f)
                            .offset(x = 1.dp, y = 1.dp)
                            .graphicsLayer { clip = false },
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = day.toString(),
                        color = highlightTextColor,
                        modifier = textModifier
                    )
                }
            }

            isToday -> {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(1.dp, todayBorderColor)
                    )
                    Text(
                        text = day.toString(),
                        color = defaultTextColor,
                        modifier = textModifier
                    )
                }
            }

            else -> {
                Text(
                    text = day.toString(),
                    color = defaultTextColor,
                )
            }
        }
    }
}


private fun currentDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

private fun daysInMonth(year: Int, month: Int): Int {
    val date = LocalDate(year, month, 1)
    val nextMonth = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
    return (nextMonth.toEpochDays() - date.toEpochDays())
}




