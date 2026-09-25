package org.timer.main.domain


@JsFun("() => Intl.DateTimeFormat().resolvedOptions().timeZone")
private external fun getBrowserTimeZone(): String

actual fun platformTimeZoneId(): String {
    return try {
        getBrowserTimeZone()
    } catch (e: Throwable) {
        "UTC"
    }
}