package org.timer

import org.koin.core.module.*
import org.koin.dsl.*
import org.timer.main.domain.summary.*
import org.timer.main.timer.*

actual fun platformSpecificModule(): Module= module {
    single<AlarmPlayer> { DefaultAlarmPlayer() }
    factory<MobileAlarm> { DefaultMobileAlarm() }
    single<SummaryGateway> { UnavailableSummaryGateway() }
}