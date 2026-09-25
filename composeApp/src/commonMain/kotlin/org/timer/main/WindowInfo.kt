package org.timer.main

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.*

class WindowInfo(val size: Size)

@Composable
expect fun rememberWindowInfo(): WindowInfo

fun WindowInfo.isSmallScreen() = size.width < 600

expect fun isWeb(): Boolean

expect fun isIOS(): Boolean

expect fun isMobile(): Boolean
