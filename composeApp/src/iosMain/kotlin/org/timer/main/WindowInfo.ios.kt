package org.timer.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberWindowInfo(): WindowInfo {
    val screen = UIScreen.mainScreen
    val width = screen.bounds.useContents { size.width }.toFloat()
    val height = screen.bounds.useContents { size.height }.toFloat()
    return remember { WindowInfo(Size(width, height)) }
}

actual fun isWeb(): Boolean = false
actual fun isIOS(): Boolean =true
actual fun isMobile()= true