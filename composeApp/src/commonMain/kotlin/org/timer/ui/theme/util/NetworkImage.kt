package org.timer.ui.theme.util

import androidx.compose.runtime.*
import androidx.compose.ui.*

@Composable
expect fun NetworkImage(
    url: String,
    modifier: Modifier,
    contentDescription: String?
)