package org.timer.main

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.*
import kotlinx.browser.*

@Composable
actual fun rememberWindowInfo(): WindowInfo {
    val width = window.innerWidth.toFloat()
    val height = window.innerHeight.toFloat()
    return remember { WindowInfo(Size(width, height)) }
}

actual fun isWeb() = true
actual fun isIOS(): Boolean = false
actual fun isMobile() = false