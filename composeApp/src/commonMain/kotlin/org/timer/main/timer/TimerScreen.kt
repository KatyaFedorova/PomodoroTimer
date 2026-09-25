package org.timer.main.timer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.*
import kotlinx.coroutines.*
import org.koin.compose.viewmodel.*
import org.timer.main.*
import org.timer.main.progress.header.*
import org.timer.main.projects.*


@ExperimentalMaterial3Api
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = koinViewModel(),
    windowInfo: WindowInfo = rememberWindowInfo(),
    navigateToStatisticScreen: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    observeScreenLifecicle(viewModel)
    AskNotificationPermission()
    if (windowInfo.isSmallScreen()) {
        ProjectsScreen(
            timerViewModel = viewModel,
            navigateToKittyScreen = navigateToStatisticScreen
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            CatBowlProgressHeader(navigateToStatisticScreen = navigateToStatisticScreen)
            Spacer(modifier = Modifier.height(if (windowInfo.isSmallScreen()) 48.dp else 8.dp))
            TimerPager(viewModel, viewState)
        }

    }
}

@Composable
expect fun AskNotificationPermission()

@Composable
private fun observeScreenLifecicle(viewModel: TimerViewModel) {
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

@ExperimentalMaterial3Api
@Composable
fun TimerPager(
    viewModel: TimerViewModel,
    viewState: TimerViewState,
    windowInfo: WindowInfo = rememberWindowInfo()
) {
    val titles = listOf("Kittydoro", "Short Break", "Long Break")
    val pagerState = rememberPagerState(pageCount = { titles.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.targetPage }.collect { page ->
            viewModel.onPageChanged(page)
        }
    }
    LaunchedEffect(viewState) {
        pagerState.animateScrollToPage(viewState.selectedTabIndex)
    }

    Row(
        modifier = if (windowInfo.isSmallScreen()) Modifier.wrapContentSize() else Modifier.fillMaxSize()
    ) {
        ElevatedCard(
            modifier = if (windowInfo.isSmallScreen()) {
                Modifier.wrapContentSize()
            } else {
                Modifier.fillMaxSize()
            },
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        ) {
            Column(
                modifier = if (windowInfo.isSmallScreen()) {
                    Modifier.wrapContentSize()
                } else {
                    Modifier.fillMaxSize()
                }
            ) {
                SecondaryTabRow(
                    selectedTabIndex = viewState.selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = viewState.selectedTabIndex == index,
                            onClick = {
                                viewModel.onTabClick(index)
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            text = {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = if (windowInfo.isSmallScreen()) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    }
                ) { page ->
                    Row(
                        modifier = if (windowInfo.isSmallScreen()) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.fillMaxSize()
                        },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (page) {
                            0 -> KittidoroContent(viewState, viewModel)
                            1 -> ShortBreakContent(viewState, viewModel)
                            2 -> LongBreakContent(viewState, viewModel)
                        }
                    }
                }

                Text(
                    text = "Kittydoros: ${viewState.kittyDoroNumber}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}


@Composable
private fun KittidoroContent(
    viewState: TimerViewState,
    viewModel: TimerViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Keep the eye on the ball!",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = viewState.pomodoroTime,
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        Row {
            KittydoroStartResetButton(viewState, viewModel)
        }
    }
}

@Composable
fun KittydoroStartResetButton(
    viewState: TimerViewState,
    viewModel: TimerViewModel,
    lazyListState: LazyListState? = null,
) {
    val scope = rememberCoroutineScope()

    if (viewState.isPomodoroStartVisible) {
        Button(
            onClick = {
                viewModel.onPomodoroStartClick()
                scrollToBeginningOfList(lazyListState, scope)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Start")
        }
    }

    if (viewState.isPomodoroPauseVisible) {
        Button(
            onClick = { viewModel.onResetClick() },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Reset")
        }
    }
}

private fun scrollToBeginningOfList(
    lazyListState: LazyListState?,
    scope: CoroutineScope
) {
    lazyListState?.let { state ->
        scope.launch {
            state.animateScrollToItem(index = 0)
        }
    }
}

@Composable
private fun ShortBreakContent(viewState: TimerViewState, viewModel: TimerViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Take a break Kitty",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(8.dp),
        )
        Text(
            text = viewState.shortBreakTime,
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        Row {
            ShortBreakStartResetButton(viewState, viewModel)
        }
    }
}

@Composable
fun ShortBreakStartResetButton(
    viewState: TimerViewState,
    viewModel: TimerViewModel,
    lazyListState: LazyListState? = null,
) {
    val scope = rememberCoroutineScope()
    if (viewState.isShortBreakStartVisible) {
        Button(
            onClick = {
                viewModel.onShortBreakStartClick()
                scrollToBeginningOfList(lazyListState, scope)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Start")
        }
    }

    if (viewState.isShortBreakPauseVisible) {
        Button(
            onClick = { viewModel.onResetClick() },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Reset")
        }
    }
}


@Composable
fun LongBreakContent(viewState: TimerViewState, viewModel: TimerViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Take a long break Kitty",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(8.dp),
        )
        Text(
            text = viewState.longBreakTime,
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        Row {
            LongBreakStartResetButton(viewState, viewModel)
        }
    }
}

@Composable
fun LongBreakStartResetButton(
    viewState: TimerViewState,
    viewModel: TimerViewModel,
    lazyListState: LazyListState? = null,
) {
    val scope = rememberCoroutineScope()

    if (viewState.isLongBreakStartVisible) {
        Button(
            onClick = {
                viewModel.onLongBreakStartClick()
                scrollToBeginningOfList(lazyListState, scope)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Start")
        }
    }

    if (viewState.isLongBreakPauseVisible) {
        Button(
            onClick = { viewModel.onResetClick() },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Reset")
        }
    }
}


