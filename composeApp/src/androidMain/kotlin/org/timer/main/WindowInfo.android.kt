package org.timer.main

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*

@Composable
actual fun rememberWindowInfo(): WindowInfo {
    val (height, width) = LocalConfiguration.current.run { screenHeightDp.dp to screenWidthDp.dp }
    return remember { WindowInfo(Size(width.value, height.value)) }
}

actual fun isWeb() = false
actual fun isIOS(): Boolean = false
actual fun isMobile() = true