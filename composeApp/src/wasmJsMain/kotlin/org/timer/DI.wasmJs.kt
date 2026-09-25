package org.timer

import org.timer.main.auth.*

actual fun afterKoinStarted() {
    registerMagicLinkHandler()
}