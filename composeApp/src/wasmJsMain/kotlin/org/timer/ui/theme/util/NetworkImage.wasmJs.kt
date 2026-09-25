package org.timer.ui.theme.util

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import coil3.compose.*

@Composable
actual fun NetworkImage(
    url: String,
    modifier: Modifier,
    contentDescription: String?
) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.FillWidth
    )
}