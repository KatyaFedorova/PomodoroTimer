import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel
import org.timer.main.feedback.FeedbackViewModel
import org.timer.main.feedback.FeedbackViewState

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit
) {
    val key = remember { Clock.System.now().toEpochMilliseconds().toString() }
    val viewModel: FeedbackViewModel = koinViewModel(key = key)
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(Modifier.padding(20.dp)) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }

                        if (viewState.isSuccess) {
                            SuccessContent(onDismiss = onDismiss)
                        }else {
                            SendFeedbackContent(
                            stars = viewState.stars,
                            massage = viewState.massage,
                            onMassageChanged = viewModel::onMassageChanged,
                            isSendEnabled = viewState.isSendEnabled,
                            onStarClick = viewModel::onStarClick,
                            onSendFeedback = viewModel::onSendFeedbackClick,
                        )

                }
            }
        }
    }
}

@Composable
private fun SendFeedbackContent(
    stars: Int,
    massage: String,
    onMassageChanged: (String) -> Unit,
    isSendEnabled: Boolean,
    onStarClick: (Int) -> Unit,
    onSendFeedback: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "We value your opinion",
            style = MaterialTheme.typography.titleLarge
        )

        Text("How would you rate your overall experience?")

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { i ->
                val filled = i <= stars
                IconButton(onClick = { onStarClick(i) }) {
                    Icon(
                        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Mark: $i",
                        tint = if (filled) Color(0xFFFFD700)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Text("We worship “user driven feature development“ and want to hear what you like and dislike about our product.")
        val keyboardController = LocalSoftwareKeyboardController.current

        OutlinedTextField(
            value = massage,
            onValueChange = { onMassageChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            singleLine = false,
            maxLines = 6,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            )
        )

        Button(
            onClick = {
                onSendFeedback()
                keyboardController?.hide()
            },
            enabled = isSendEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share my feedback")
        }
    }
}

@Composable
private fun SuccessContent(
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF4CAF50), //green
            modifier = Modifier.size(96.dp)
        )

        Text(
            text = "Purr-fect! Thanks for your feedback",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onDismiss,
        ) {
            Text("Close")
        }
    }
}
