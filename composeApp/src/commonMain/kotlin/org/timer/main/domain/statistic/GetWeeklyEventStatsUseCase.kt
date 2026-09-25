package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import org.timer.main.domain.*

interface GetWeeklyEventStatsUseCase {
    operator fun invoke(weekOffset: Int, eventTypes: List<EventType>): Flow<WeeklyEventStats>
}

class DefaultGetWeeklyEventStatsUseCase(
    private val gateway: StatisticsEventGateway,
    private val clock: SystemClock
) : GetWeeklyEventStatsUseCase {

    override operator fun invoke(weekOffset: Int, eventTypes: List<EventType>): Flow<WeeklyEventStats> {
        val todayInstant = Instant.fromEpochMilliseconds(clock.now())
        val tz = TimeZone.currentSystemDefault()
        val today = todayInstant.toLocalDateTime(tz).date

        val startOfWeek = today
            .minus((today.dayOfWeek.ordinal + 1) % 7, DateTimeUnit.DAY)
            .plus(weekOffset * 7, DateTimeUnit.DAY)

        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)

        val fromMillis = startOfWeek.atStartOfDayIn(tz).toEpochMilliseconds()
        val toMillis = endOfWeek.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).toEpochMilliseconds()

        return gateway.getEventsInRange(fromMillis, toMillis)
            .map { events ->
                val dailyStats = (0..6).map { i ->
                    val date = startOfWeek.plus(i, DateTimeUnit.DAY)
                    val count = events.count { event ->
                        event.timestamp != null &&
                                eventTypes.contains(event.eventType) &&
                                Instant.fromEpochMilliseconds(event.timestamp)
                                    .toLocalDateTime(tz).date == date
                    }
                    DailyEventStat(date, count)
                }
                WeeklyEventStats(
                    weekStart = startOfWeek,
                    weekEnd = endOfWeek,
                    daily = dailyStats
                )
            }
    }
}

data class WeeklyEventStats(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val daily: List<DailyEventStat>
)

data class DailyEventStat(
    val date: LocalDate,
    val count: Int
)
