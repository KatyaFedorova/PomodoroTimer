package org.timer.main.breakactivity

import android.app.*
import android.content.pm.*
import android.os.*
import android.view.*
import android.view.WindowInsets
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.viewinterop.*
import androidx.lifecycle.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.*


@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    onWatched70Percent: () -> Unit,
    onVideoEnded: () -> Unit
) {
    YoutubeVideoPlayer(
        modifier = modifier,
        youtubeURL = url,
        autoPlay = true,
        showControls = true,
        onVideoEnded = onVideoEnded,
        onWatched70Percent = onWatched70Percent
    )
}

@Composable
fun YoutubeVideoPlayer(
    modifier: Modifier = Modifier,
    youtubeURL: String,
    isPlaying: (Boolean) -> Unit = {},
    isLoading: (Boolean) -> Unit = {},
    onVideoEnded: () -> Unit = {},
    autoPlay: Boolean,
    showControls: Boolean,
    onWatched70Percent: () -> Unit = {}
) {
    val context = LocalContext.current
    var activity by remember { mutableStateOf<Activity?>(null) }
    LaunchedEffect(context) { activity = context as? Activity }

    val scope = rememberCoroutineScope()

    val videoId = remember(youtubeURL) {
        youtubeURL.substringAfter("v=").substringBefore("&")
            .ifEmpty { youtubeURL.substringAfterLast("/") }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var player: YouTubePlayer? = null
    val playerView = remember { YouTubePlayerView(context) }

    var isLoadingState by remember { mutableStateOf(true) }
    var fullscreenView: View? by remember { mutableStateOf(null) }
    var isFullScreen by remember { mutableStateOf(false) }

    // трекер просмотра реального времени
    val tracker = remember {
        VideoWatchTracker(
            scope = scope,
            percent = 0.7f,
            onWatchedPercent = onWatched70Percent
        )
    }

    val fullScreenListener = object : FullscreenListener {
        override fun onEnterFullscreen(view: View, exitFullscreen: () -> Unit) {
            isFullScreen = true
            fullscreenView = view
            playerView.visibility = View.GONE

            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            Handler(Looper.getMainLooper()).post {
                (activity?.window?.decorView as ViewGroup).addView(view)
                activity?.let { configureFullScreen(it, true) }
            }
            player?.play()
        }

        override fun onExitFullscreen() {
            isFullScreen = false
            playerView.visibility = View.VISIBLE
            fullscreenView?.let { view ->
                (activity?.window?.decorView as ViewGroup).removeView(view)
                fullscreenView = null
            }

            Handler(Looper.getMainLooper()).post {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity?.let { configureFullScreen(it, false) }
            }
            player?.play()
        }
    }

    val playerStateListener = object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            player = youTubePlayer
            youTubePlayer.loadVideo(videoId, 0f)
            isLoadingState = false
            isLoading(false)
        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, length: Float) {
            tracker.onDurationKnown(length)
        }

        override fun onStateChange(
            youTubePlayer: YouTubePlayer,
            state: PlayerConstants.PlayerState,
        ) {
            when (state) {
                PlayerConstants.PlayerState.PLAYING -> {
                    isLoadingState = false
                    isLoading(false)
                    isPlaying(true)
                    tracker.onPlay()
                }

                PlayerConstants.PlayerState.PAUSED,
                PlayerConstants.PlayerState.BUFFERING -> {
                    isPlaying(false)
                    tracker.onStopLike()
                    if (state == PlayerConstants.PlayerState.BUFFERING) {
                        isLoadingState = true
                        isLoading(true)
                    }
                }

                PlayerConstants.PlayerState.ENDED -> {
                    isPlaying(false)
                    isLoading(false)
                    tracker.onStopLike() // докинем последние секунды и финальная проверка
                    onVideoEnded()
                }

                else -> {}
            }
        }
    }

    val playerBuilder = IFramePlayerOptions.Builder().apply {
        controls(if (showControls) 1 else 0)
        fullscreen(1)
        autoplay(if (autoPlay) 1 else 0)
        modestBranding(1)
        rel(0)
        ivLoadPolicy(3)
        ccLoadPolicy(1)
    }

    Box(modifier = modifier.background(Color.Black)) {
        if (isLoadingState) {
            CircularProgressIndicator(
                modifier = androidx.compose.ui.Modifier
                    .align(Alignment.Center)
                    .then(androidx.compose.ui.Modifier),
                color = Color.White
            )
        }

        AndroidView(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(Color.Black)
                .alpha(if (isLoadingState) 0f else 1f),
            factory = {
                playerView.apply {
                    enableAutomaticInitialization = false
                    initialize(playerStateListener, playerBuilder.build())
                    addFullscreenListener(fullScreenListener)
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            playerView.removeYouTubePlayerListener(playerStateListener)
            playerView.removeFullscreenListener(fullScreenListener)
            playerView.release()
            tracker.release()
            player = null
        }
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> player?.play()
                Lifecycle.Event.ON_PAUSE -> player?.pause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

fun configureFullScreen(activity: Activity, enable: Boolean) {
    if (enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
            activity.window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(true)
            activity.window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}
