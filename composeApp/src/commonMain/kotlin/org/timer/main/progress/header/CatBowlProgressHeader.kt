package org.timer.main.progress.header

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.*
import org.jetbrains.compose.resources.*
import org.koin.compose.viewmodel.*
import org.timer.main.*
import pomodorotimer.composeapp.generated.resources.*

@Composable
fun CatBowlProgressHeader(
    viewModel: CatBowlProgressViewModel = koinViewModel(),
    navigateToStatisticScreen: () -> Unit = {},
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val windowInfo = rememberWindowInfo()
    var showInfoDialog by remember { mutableStateOf(false) }


    observeScreenLifecicle(viewModel)

    if (windowInfo.isSmallScreen()) {
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(elevation = 4.dp),
            color = MaterialTheme.colorScheme.secondaryContainer

        ) {
            Box {
                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(start = 0.dp, end = 4.dp, top = 4.dp, bottom = 0.dp)
                        .size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            content(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp, start = 8.dp, end = 16.dp, top = 16.dp),
                viewState,
                navigateToStatisticScreen,
                windowInfo
            )
        }
    } else {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        ) {
            content(
                modifier = Modifier
                    .fillMaxWidth().padding(bottom = 4.dp, end = 4.dp),
                viewState,
                navigateToStatisticScreen,
                windowInfo
            )
        }
    }

    // 📌 Диалог
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("How it works") },
            text = {
                Text(
                    "Each time you exercise during a break, Kitty’s bowl fills with food (see Activity screen). " +
                            "To make Kitty full today, complete 4 Kittidoros + 4 active breaks. " +
                            "Extra Kittidoros with workouts are saved to feed your real kitty later."
                )
            }
        )
    }
}

@Composable
private fun observeScreenLifecicle(viewModel: CatBowlProgressViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
private fun content(
    modifier: Modifier,
    viewState: TimerViewState,
    navigateToStatisticScreen: () -> Unit,
    windowInfo: WindowInfo
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 🐱 Котик
        Box(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            val cat =
                if (viewState.isBowlFull) Res.drawable.cat_happy_new else Res.drawable.cat_greem_new
            Image(
                painter = painterResource(cat),
                contentDescription = "Cat bowl",
                modifier = Modifier.size(38.dp).offset(y = 5.dp),
                contentScale = ContentScale.Fit,
            )
        }

        // 👉 Текст + прогресс
        Column(
            modifier = Modifier.weight(1f)
        ) {
            if (viewState.isBowlFull) {
                val fullText = "Bowl is full! Click here to see statistic!"
                val clickableTextPart = "Click here"
                val annotatedString = buildAnnotatedString {
                    append(fullText.substringBefore(clickableTextPart))
                    pushStringAnnotation(
                        tag = "FEED_KITTY_ACTION",
                        annotation = "feed_kitty"
                    )
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                        )
                    ) {
                        append(clickableTextPart)
                    }
                    pop()
                    append(fullText.substringAfter(clickableTextPart))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClickableText(
                        text = annotatedString,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(
                                tag = "FEED_KITTY_ACTION",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let {
                                navigateToStatisticScreen.invoke()
                            }
                        }
                    )

                    if (!windowInfo.isSmallScreen()) {
                        IconButton(
                            onClick = { showInfoDialog = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Do exercises during the break to feed Kitty",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    if (!windowInfo.isSmallScreen()) {
                        IconButton(
                            onClick = { showInfoDialog = true },
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 📊 Прогрессбар + проценты внутри
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LinearProgressIndicator(
                        progress = { viewState.fillingProgress },
                        modifier = Modifier.fillMaxSize(),
                        gapSize = 0.dp,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        drawStopIndicator = {}
                    )
                    Text(
                        text = "${((viewState.fillingProgress) * 100).toInt()} / 100%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }
    }

    // 👇 Вынесено наружу: диалог всегда в дереве
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("How it works") },
            text = {
                Text(
                    "Each time you exercise during a break, Kitty’s bowl fills with food (see Activity screen). " +
                            "To make Kitty full today, complete 4 Kittidoros + 4 active breaks. " +
                            "Extra Kittidoros with workouts are saved to feed your real kitty later."
                )
            }
        )
    }
}
