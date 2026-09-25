package org.timer.ui.theme.util

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import io.kamel.image.*


@Composable
actual fun NetworkImage(
    url: String,
    modifier: Modifier,
    contentDescription: String?
) {
    KamelImage(
        resource = { asyncPainterResource(url) },
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.FillWidth
    )
}