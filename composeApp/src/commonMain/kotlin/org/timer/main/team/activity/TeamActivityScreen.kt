package org.timer.main.team.activity

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import org.jetbrains.compose.ui.tooling.preview.*
import org.koin.compose.viewmodel.*
import org.timer.main.*
import org.timer.main.progress.header.*

@Composable
fun TeamActivityScreen(
    viewModel: TeamActivityViewModel = koinViewModel(),
    onBackPress: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val windowInfo = rememberWindowInfo()

    if (windowInfo.isSmallScreen()) {
        Column {
            CatBowlProgressHeader()
            Spacer(Modifier.height(8.dp))
            TeamActivityContent(
                members = viewState.members,
                isSmallScreen = true,
                onBackPress = onBackPress
            )
        }
    } else {
        Row(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            ) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    TeamActivityContent(
                        members = viewState.members,
                        isSmallScreen = false,
                        onBackPress = onBackPress
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamActivityContent(
    members: List<PresentableTeamMember>,
    isSmallScreen: Boolean,
    onBackPress: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isSmallScreen) 0.dp else 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isSmallScreen) {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Text(
                    text = "Team fellas' status",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                if (!isSmallScreen) {
                    Spacer(modifier = Modifier.width(48.dp)) // баланс справа
                }
            }

            Spacer(Modifier.height(12.dp))

            if (members.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No team fellas yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = members,
                        key = { it.id }
                    ) { member ->
                        MemberCard(member = member, isSmallScreen = isSmallScreen)
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(member: PresentableTeamMember, isSmallScreen: Boolean) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isSmallScreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                StatusChip(getActiveStatus(member))
                StatsColumn(member)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatusChip(getActiveStatus(member))
                    StatsRow(member)
                }
            }
        }
    }
}

private fun getActiveStatus(member: PresentableTeamMember): Pair<String, Color> {
    return when {
        member.onPomodoro -> "Pomodoro session (do not disturb)" to Color.Red
        member.onShortBreak -> "On a short break" to Color.Green
        member.onLongBreak -> "On a long break" to Color.Green
        member.aviable -> "Not in the session" to  Color.Green
        else -> "Idle" to Color.Gray
    }
}

@Composable
private fun StatusChip(status: Pair<String, Color>) {
    val (label, dotColor) = status
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics { contentDescription = "Status: $label" }
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatsColumn(member: PresentableTeamMember) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        StatChip("Pomodoros done today", member.kittidorosCount.toString())
        StatChip("Exercises done today", member.exercisesCount.toString())
    }
}

@Composable
private fun StatsRow(member: PresentableTeamMember) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatChip("Pomodoros: ", member.kittidorosCount.toString())
        StatChip("Exercises: ", member.exercisesCount.toString())
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Text(
        text = "$label $value",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}