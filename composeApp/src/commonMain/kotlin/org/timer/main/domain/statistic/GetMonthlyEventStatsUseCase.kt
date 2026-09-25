package org.timer.main.domain.statistic

import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import org.timer.main.domain.*


interface GetMonthlyEventStatsUseCase {
    operator fun invoke(monthOffset: Int, eventTypes: List<EventType>): Flow<MonthlyEventStats>
}

class DefaultGetMonthlyEventStatsUseCase(
    private val gateway: StatisticsEventGateway,
    private val clock: SystemClock
) : GetMonthlyEventStatsUseCase {

    override operator fun invoke(monthOffset: Int, eventTypes: List<EventType>): Flow<MonthlyEventStats> {
        val tz = TimeZone.currentSystemDefault()
        val today = Instant.fromEpochMilliseconds(clock.now()).toLocalDateTime(tz).date

        val currentMonthStart = LocalDate(today.year, today.month, 1)
        val monthStart = currentMonthStart.plus(monthOffset, DateTimeUnit.MONTH)
        val nextMonthStart = monthStart.plus(1, DateTimeUnit.MONTH)
        val monthEnd = nextMonthStart.minus(1, DateTimeUnit.DAY)

        val fromMillis = monthStart.atStartOfDayIn(tz).toEpochMilliseconds()
        val toMillis = nextMonthStart.atStartOfDayIn(tz).toEpochMilliseconds()

        return gateway.getEventsInRange(fromMillis, toMillis).map { events ->
            val days = buildList {
                var d = monthStart
                while (d < nextMonthStart) {
                    add(d)
                    d = d.plus(DatePeriod(days = 1))
                }
            }

            val dailyStats = days.map { date ->
                val count = events.count { e ->
                    e.timestamp != null &&
                            eventTypes.contains(e.eventType) &&
                            Instant.fromEpochMilliseconds(e.timestamp)
                                .toLocalDateTime(tz).date == date
                }
                DailyEventStat(date = date, count = count)
            }

            MonthlyEventStats(
                monthStart = monthStart,
                monthEnd = monthEnd,
                daily = dailyStats
            )
        }
    }
}

data class MonthlyEventStats(
    val monthStart: LocalDate,
    val monthEnd: LocalDate,
    val daily: List<DailyEventStat>
)
