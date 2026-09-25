package org.timer.main.domain

import kotlinx.datetime.*

actual fun platformTimeZoneId(): String {
    return TimeZone.currentSystemDefault().id
}