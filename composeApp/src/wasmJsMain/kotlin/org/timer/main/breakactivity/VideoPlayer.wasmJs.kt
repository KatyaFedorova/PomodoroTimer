package org.timer.main.breakactivity

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.diamondedge.logging.logging
import kotlinx.browser.document
import org.timer.main.*
import org.w3c.dom.HTMLDivElement

@JsName("myYouTubeBridge")
external object MyYouTubeBridge {
    fun createPlayer(
        containerId: String,
        videoId: String,
        onWatched30Seconds: () -> Unit,
        onEnded: () -> Unit
    )
    fun destroyPlayer(containerId: String)
}

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    onWatched70Percent: () -> Unit,
    onVideoEnded: () -> Unit
) {
    val videoId = remember(url) { extractVideoId(url) }
    val containerId = remember { "yt-player-container" }

    // Контейнер для YouTube iframe
    Box(modifier = modifier.fillMaxSize()) {
        HtmlView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                val div = document.createElement("div") as HTMLDivElement
                div.id = containerId
                div
            }
        )
    }

    // Создание / пересоздание плеера при смене видео
    LaunchedEffect(videoId) {
        try { MyYouTubeBridge.destroyPlayer(containerId) } catch (_: Throwable) {}
        try {
            MyYouTubeBridge.createPlayer(containerId, videoId, {
                onWatched70Percent()
            }, {
                onVideoEnded()
            })
        } catch (e: Throwable) {
            logging().e(e) { "createPlayer error" }
        }
    }

    // Разрушение плеера при уходе composable
    DisposableEffect(Unit) {
        onDispose {
            try { MyYouTubeBridge.destroyPlayer(containerId) } catch (e: Throwable) {
                logging().e(e) { "destroyPlayer error" }
            }
        }
    }
}

private fun extractVideoId(url: String): String {
    val videoIdRegex =
        Regex("""(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})""")
    return videoIdRegex.find(url)?.groupValues?.get(1) ?: "dQw4w9WgXcQ"
}
