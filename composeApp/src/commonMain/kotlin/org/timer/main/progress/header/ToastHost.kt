import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest
import org.timer.main.progress.header.*

@Composable
fun ToastHost(state: ToastHostState, modifier: Modifier = Modifier) {
    var currentToast by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current
    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) } // 👈 начинаем с нормального размера
    var toastHeightPx by remember { mutableStateOf(0) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeightPx = with(density) { maxHeight.toPx() }

        LaunchedEffect(state, screenHeightPx, toastHeightPx) {
            state.messages.collectLatest { msg ->
                currentToast = msg

                // сбрасываем позицию и размер
                offsetY.snapTo(screenHeightPx)
                scale.snapTo(1f)

                coroutineScope {
                    launch {
                        offsetY.animateTo(
                            targetValue = -toastHeightPx.toFloat(),
                            animationSpec = tween(
                                durationMillis = 4000,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                    launch {
                        scale.animateTo(
                            targetValue = 1.2f, // 👈 увеличиваем немного
                            animationSpec = tween(
                                durationMillis = 2000,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                }

                currentToast = null
            }
        }

        currentToast?.let { message ->
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .onGloballyPositioned { coords ->
                        toastHeightPx = coords.size.height
                    }
                    .offset { IntOffset(0, offsetY.value.roundToInt()) }
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value
                    )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
