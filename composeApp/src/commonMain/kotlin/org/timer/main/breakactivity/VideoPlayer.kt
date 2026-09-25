package org.timer.main.breakactivity

import androidx.compose.runtime.*
import androidx.compose.ui.*

@Composable
expect fun VideoPlayer(
    modifier: Modifier,
    url: String,
    onWatched70Percent: () -> Unit = {},
    onVideoEnded: () -> Unit
)