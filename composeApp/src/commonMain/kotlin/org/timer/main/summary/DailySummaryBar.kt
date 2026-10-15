package org.timer.main.summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import org.koin.compose.viewmodel.*
import org.timer.main.domain.summary.*

/** Always-visible bar pinned to the top of the web screen; clicking it opens today's AI summary. */
@Composable
fun DailySummaryBar(
    modifier: Modifier = Modifier,
    viewModel: DailySummaryViewModel = koinViewModel(),
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "How did today go? Get a quick recap of your focus, tasks and break activities.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Button(onClick = viewModel::onSummaryClick) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Praise me")
            }
        }
    }

    if (viewState.isDialogVisible) {
        DailySummaryDialog(
            viewState = viewState,
            onRetry = viewModel::onRetryClick,
            onDismiss = viewModel::onDismiss,
        )
    }
}

@Composable
private fun DailySummaryDialog(
    viewState: DailySummaryViewState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
        title = { Text("Your day so far") },
        text = {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                viewState.activity?.let { ActivityStats(it) }
                when {
                    viewState.isLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Writing your summary…")
                    }
                    viewState.error != null -> Text(
                        text = "Couldn't get your summary right now.\n${viewState.error}",
                        color = MaterialTheme.colorScheme.error,
                    )
                    viewState.summary != null -> Text(
                        text = viewState.summary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = {
            TextButton(onClick = onRetry, enabled = !viewState.isLoading) { Text("Refresh") }
        },
    )
}

@Composable
private fun ActivityStats(activity: DailyActivity) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Today", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text("Focus sessions: ${activity.pomodorosCompleted}")
        Text("Tasks completed: ${activity.tasksCompleted}")
        Text(
            "Break activities: ${activity.breakActivities} " +
                "(high-energy ${activity.highEnergyExercises}, low-energy ${activity.lowEnergyExercises}, " +
                "dance ${activity.dances}, clear mind ${activity.clearYourMindSessions})"
        )
    }
}
