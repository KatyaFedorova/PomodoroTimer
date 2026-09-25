import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.layout.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeToDeleteContainer(
    modifier: Modifier = Modifier,
    freezeSwipe: Boolean = false,
    onDismissed: () -> Unit,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Red,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    var itemWidth by remember { mutableStateOf(1) }

    val dragState = rememberDraggableState { delta ->
        if (!freezeSwipe) {
            val newOffset = (offset.value + delta).coerceAtMost(0f)
            scope.launch { offset.snapTo(newOffset) }
        }
    }

    Box(
        modifier = modifier.onSizeChanged { itemWidth = it.width }
    ) {
        val progress = if (itemWidth > 0) (-offset.value / itemWidth.toFloat()).coerceIn(0f, 1f) else 0f

        // фон и текст Delete
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(clip = true, shape = shape)
                .background(backgroundColor)
        ) {
            val offsetPx = -progress * itemWidth / 2f
            val offsetDp = with(LocalDensity.current) { offsetPx.toDp() }
            Text(
                text = "Delete",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = offsetDp)

            )
        }

        // контент
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .graphicsLayer(clip = true, shape = shape)
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        scope.launch {
                            val shouldDismiss =
                                offset.value < -itemWidth * 0.8f || velocity < -1500f
                            if (shouldDismiss && !freezeSwipe) {
                                // если диалог не показан, вызываем колбек
                                offset.animateTo(-itemWidth.toFloat(), tween(200))
                                onDismissed()
                            } else if (!freezeSwipe) {
                                // возвращаемся назад
                                offset.animateTo(0f, spring(stiffness = 600f))
                            }
                        }
                    }
                )
        ) {
            content()
        }

        // эффект возврата, когда showDialog стал false
        LaunchedEffect(freezeSwipe) {
            if (!freezeSwipe) {
                offset.animateTo(0f, spring(stiffness = 600f))
            }
        }
    }
}
