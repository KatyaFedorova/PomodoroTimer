package org.timer.main.breakactivity

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.viewinterop.*
import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.UIKit.*
import platform.WebKit.*
import platform.darwin.NSObject
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    onWatched70Percent: () -> Unit,
    onVideoEnded: () -> Unit
) {
    val videoId = remember(url) {
        url.substringAfter("v=").substringBefore("&").ifEmpty { url.substringAfterLast("/") }
    }

    val scriptMessageHandlerName = remember { "youtubePlayerHandler_${Random.nextInt().toString().replace("-", "")}" }
    var webViewInstance: WKWebView? by remember { mutableStateOf(null) }
    var hasNotified70 by remember(url) { mutableStateOf(false) }

    val htmlContent = remember(videoId, scriptMessageHandlerName) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <style>
                body, html { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: black; }
                #player_placeholder { width: 100%; height: 100%; }
            </style>
            <script src="https://www.youtube.com/iframe_api"></script>
        </head>
        <body>
            <div id="player_placeholder"></div>
            <script type="text/javascript">
                var player;
                var playerTimer;
                var watchedTime = 0;
                var jsHasNotified70 = false;
                var duration = 0;

                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player_placeholder', {
                        videoId: '$videoId',
                        width: '100%',
                        height: '100%',
                        playerVars: {
                            'autoplay': 1,
                            'controls': 1,
                            'playsinline': 1,
                            'mute': 0, 
                            'modestbranding': 1,
                            'rel': 0,
                            'showinfo': 0
                        },
                        events: {
                            'onReady': function(event) {
                                duration = player.getDuration();
                            },
                            'onStateChange': function(event) {
                                if (event.data == YT.PlayerState.PLAYING) {
                                    clearInterval(playerTimer);
                                    playerTimer = setInterval(function() {
                                        if (player && player.getPlayerState() == YT.PlayerState.PLAYING) {
                                            watchedTime++;
                                            if (!jsHasNotified70 && duration > 0 && watchedTime >= duration * 0.7) {
                                                jsHasNotified70 = true;
                                                if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.$scriptMessageHandlerName) {
                                                    window.webkit.messageHandlers.$scriptMessageHandlerName.postMessage("watched70Percent");
                                                }
                                            }
                                        } else {
                                            clearInterval(playerTimer);
                                        }
                                    }, 1000);
                                } else {
                                    clearInterval(playerTimer);
                                }

                                if (event.data == YT.PlayerState.ENDED) {
                                    if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.$scriptMessageHandlerName) {
                                        window.webkit.messageHandlers.$scriptMessageHandlerName.postMessage("videoEnded");
                                    }
                                }
                            }
                        }
                    });
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    DisposableEffect(webViewInstance, onWatched70Percent, onVideoEnded, scriptMessageHandlerName, url) {
        val currentWebView = webViewInstance
        var scriptHandler: WKScriptMessageHandlerProtocol? = null

        if (currentWebView != null) {
            hasNotified70 = false
            scriptHandler = object : NSObject(), WKScriptMessageHandlerProtocol {
                override fun userContentController(
                    userContentController: WKUserContentController,
                    didReceiveScriptMessage: WKScriptMessage
                ) {
                    if (didReceiveScriptMessage.name == scriptMessageHandlerName) {
                        val messageBody = didReceiveScriptMessage.body as? String
                        when (messageBody) {
                            "watched70Percent" -> {
                                if (!hasNotified70) {
                                    onWatched70Percent()
                                    hasNotified70 = true
                                }
                            }
                            "videoEnded" -> {
                                onVideoEnded()
                            }
                        }
                    }
                }
            }
            currentWebView.configuration.userContentController.addScriptMessageHandler(scriptHandler!!, scriptMessageHandlerName)
        }

        onDispose {
            if (currentWebView != null && scriptHandler != null) {
                currentWebView.configuration.userContentController.removeScriptMessageHandlerForName(scriptMessageHandlerName)
            }
        }
    }

    UIKitView(
        factory = {
            val configuration = WKWebViewConfiguration().apply {
                userContentController = WKUserContentController()
                allowsInlineMediaPlayback = true
                mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone
            }
            val newWebView = WKWebView(frame = CGRectZero.readValue(), configuration = configuration).apply {
                this.scrollView.scrollEnabled = false
                this.backgroundColor = UIColor.blackColor
                this.opaque = false
                this.loadHTMLString(htmlContent, baseURL = null)
            }
            webViewInstance = newWebView
            newWebView
        },
        update = { _ -> },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    )
}

