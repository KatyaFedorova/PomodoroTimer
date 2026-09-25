package org.timer.main.statistic

import Calendar
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import org.jetbrains.compose.resources.*
import org.kodein.emoji.*
import org.kodein.emoji.animals_nature.animal_mammal.*
import org.kodein.emoji.compose.*
import org.koin.compose.viewmodel.*
import org.timer.main.*
import org.timer.main.progress.header.*

@Composable
fun KittyScreen(
    viewModel: StatisticViewModel = koinViewModel(),
    onBackPress: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    val windowInfo = rememberWindowInfo()

    if (windowInfo.isSmallScreen()) {
        // 📱 На маленьких экранах всё скроллим целиком
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CalendarSection(viewState, windowInfo, onBackPress)
            Spacer(modifier = Modifier.height(4.dp))
            FishManagerSection(viewState)
        }
    } else {
        // 💻 Десктоп/большой экран — делим на две карточки
        Row(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight()
                    .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    CalendarSection(viewState, windowInfo, onBackPress)
                    Spacer(modifier = Modifier.height(4.dp))
                    FishManagerSection(viewState)
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight()
                    .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            ) {
               PlotContent()
            }
        }
    }
}



@Composable
private fun CalendarSection(
    viewState: StatisticViewState,
    windowInfo: WindowInfo,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CatBowlProgressHeader()

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (!windowInfo.isSmallScreen()) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            val text = "Days kitty was full ${Emoji.PawPrints}"
            WithPlatformEmoji(text) { textWithEmoji, inlineContent ->
                Text(
                    text = textWithEmoji,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    inlineContent = inlineContent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!windowInfo.isSmallScreen()) {
                Spacer(modifier = Modifier.width(48.dp)) // баланс справа
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Calendar(
            viewState.feedingDates,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun FishManagerSection(viewState: StatisticViewState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Fish manager",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth() // 👈 центрируем по всей ширине
        )

        if (viewState.showFishNumberText) {
            Text(
                text = "Total fishies: ${viewState.totalFishes}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (!viewState.showFishNumberText) {
            Image(
                painter = painterResource(viewState.fishImageRes),
                contentDescription = "Fishes",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
