package org.timer.main.projects

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.DpSize

@Composable
fun ShortLongPress(content: @Composable () -> Unit) {
    val timeoutMs = 200L
    val base = LocalViewConfiguration.current
    val custom = remember(base, timeoutMs) {
        object : ViewConfiguration {
            override val longPressTimeoutMillis: Long = timeoutMs
            override val doubleTapTimeoutMillis: Long = base.doubleTapTimeoutMillis
            override val doubleTapMinTimeMillis: Long = base.doubleTapMinTimeMillis
            override val touchSlop: Float = base.touchSlop
            override val maximumFlingVelocity: Float = base.maximumFlingVelocity
            override val minimumTouchTargetSize: DpSize = base.minimumTouchTargetSize
        }
    }
    CompositionLocalProvider(LocalViewConfiguration provides custom, content = content)
}
