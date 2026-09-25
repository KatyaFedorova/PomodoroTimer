package org.timer.main.breakactivity

import FeedbackDialog
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import org.jetbrains.compose.resources.*
import org.kodein.emoji.compose.*
import org.koin.compose.viewmodel.*
import org.timer.main.*
import org.timer.main.WindowInfo
import org.timer.main.domain.video.*
import org.timer.main.progress.header.*
import org.timer.main.settings.*
import org.timer.main.timer.*
import org.timer.ui.theme.util.*
import pomodorotimer.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakActivityScreen(
    timerViewModel: TimerViewModel = koinViewModel(),
    windowInfo: WindowInfo = rememberWindowInfo(),
    viewModel: BreakActivityViewModel = koinViewModel(),
    navigateToStatisticScreen: () -> Unit = {},
    navigateToTimerScreen: () -> Unit = {},
    navigateToTeamActivityScreen: () -> Unit = {},
) {

    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val timerViewState by timerViewModel.viewState.collectAsStateWithLifecycle()
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(timerViewState.timerState) {
        viewModel.onTimerStateChanged(timerViewState.timerState)
    }

    LaunchedEffect(timerViewState.kittyDoroNumber) {
        viewModel.onKittdoroNuamberChanged(timerViewState.kittyDoroNumber)
    }

    LaunchedEffect(Unit) {
        timerViewModel.tabClicks.collect { index ->
            viewModel.onTimerTabClick(index)
        }
    }

    LaunchedEffect(timerViewState.isShortBreakTimerRunning) {
        viewModel.onShortBreakTimerRunningStateChanged(timerViewState.isShortBreakTimerRunning)
    }

    LaunchedEffect(timerViewState.isPomodoroTimerRunning) {
        if (timerViewState.isPomodoroTimerRunning) viewModel.onPomodoroTimerStartRunning()
    }

    LaunchedEffect(viewState.navigateToTimerScreen) {
        if (viewState.navigateToTimerScreen) navigateToTimerScreen()
        viewModel.onNavigatedToTimerScreen()
    }

    LaunchedEffect(viewState.startShortBreak) {
        if (viewState.startShortBreak) timerViewModel.onShortBreakStartClick()
        viewModel.onShortBreakStarted()
    }
    LaunchedEffect(viewState.startLongBreak) {
        if (viewState.startLongBreak) timerViewModel.onLongBreakStartClick()
        viewModel.onLongBreakStarted()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (!windowInfo.isSmallScreen()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (viewState.showSettingsHint) {
                    // 🔹 пока хинт показывается — отключаем тултипы у всех иконок
                    PlainIconButton(onClick = { navigateToTeamActivityScreen() }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.team),
                            contentDescription = "Team"
                        )
                    }
                    PlainIconButton(onClick = { navigateToStatisticScreen() }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.kitty),
                            contentDescription = "Statistics"
                        )
                    }
                    PlainIconButton(onClick = {
                        showDialog.value = true
                        viewModel.onSettingsHintDismissed()
                    }
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                } else {
                    // 🔹 обычные тултипы
                    TooltipIconButton(
                        label = "Team",
                        onClick = { navigateToTeamActivityScreen() },
                        icon = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.team),
                                contentDescription = "Team"
                            )
                        }
                    )
                    TooltipIconButton(
                        label = "Statistics",
                        onClick = { navigateToStatisticScreen() },
                        icon = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.kitty),
                                contentDescription = "Statistics"
                            )
                        }
                    )
                    TooltipIconButton(
                        label = "Settings",
                        onClick = { showDialog.value = true },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                    )
                }
            }
        }

        if (showDialog.value) {
            SettingsDialogScreen(isDialogVisible = { showDialog.value = it })
        }


        Column {
            if (windowInfo.isSmallScreen()) {
                CatBowlProgressHeader(navigateToStatisticScreen = navigateToStatisticScreen)
                Spacer(Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!windowInfo.isSmallScreen()) {
                    Spacer(Modifier.height(48.dp))
                }

                when (viewState.screenState) {
                    is ScreenState.PomodoroScreen -> {
                        if (windowInfo.isSmallScreen()) {
                            Title(
                                text = "Short break",
                                showBackButton = viewState.showBackButton,
                                onBackClick = { viewModel.onBackClick() },
                                currentTime = timerViewState.shortBreakTime
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ToggleBreakTypeButton(
                                selectedBreakType = timerViewState.timerState,
                                isShortBreakSelected = {
                                    if (it) timerViewModel.onShortBreakStartClick()
                                    else timerViewModel.onLongBreakStartClick()
                                }
                            )
                            MenuWithContent(viewState, showDialog, viewModel)
                        } else {
                            KittyWithLaptop()
                        }
                    }


                    is ScreenState.ShortBreakScreen -> {
                        Title(
                            text = "Short break",
                            showBackButton = viewState.showBackButton,
                            onBackClick = { viewModel.onBackClick() },
                            currentTime = timerViewState.shortBreakTime
                        )
                        if (windowInfo.isSmallScreen()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ToggleBreakTypeButton(
                                selectedBreakType = timerViewState.timerState,
                                isShortBreakSelected = {
                                    if (it) timerViewModel.onShortBreakStartClick()
                                    else timerViewModel.onLongBreakStartClick()
                                }
                            )
                        }
                        MenuWithContent(viewState, showDialog, viewModel)
                    }

                    is ScreenState.LongBreakScreen -> {
                        Title(
                            text = "Long break",
                            showBackButton = viewState.showBackButton,
                            onBackClick = { viewModel.onBackClick() },
                            currentTime = timerViewState.longBreakTime
                        )
                        if (windowInfo.isSmallScreen()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ToggleBreakTypeButton(
                                selectedBreakType = timerViewState.timerState,
                                isShortBreakSelected = {
                                    if (it) timerViewModel.onShortBreakStartClick()
                                    else timerViewModel.onLongBreakStartClick()
                                },
                            )
                        }
                        GoForIt()
                    }
                }
            }
        }

        if (viewState.showSettingsHint) {
            SettingsHintCard(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 16.dp),
                onDismiss = { viewModel.onSettingsHintDismissed() },
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val tooltipState = rememberTooltipState()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        state = tooltipState
    ) {
        IconButton(onClick = onClick) {
            icon()
        }
    }
}


@Composable
fun SettingsHintCard(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    // те же параметры стрелки внутри
    val arrowWidth = 36.dp
    val arrowHeight = 8.dp
    val arrowOffsetEnd = 14.dp

    // один общий цвет для карточки И стрелочки с учётом высоты (тональной) — ключ к совпадению
    val elevation = 6.dp
    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val strokePx = with(LocalDensity.current) { 1.dp.toPx() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // 🔺 широкая низкая стрелка, тем же цветом, что и карточка
        Canvas(
            modifier = Modifier
                .width(arrowWidth)
                .height(arrowHeight)
                .padding(end = arrowOffsetEnd)
        ) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w / 2f, 0f)  // верхняя точка
                lineTo(0f, h)       // левый низ
                lineTo(w, h)        // правый низ
                close()
            }
            drawPath(path, color = containerColor)                         // заливка как у карточки
            drawPath(path, color = outline, style = Stroke(width = strokePx)) // тонкий контур
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = containerColor,   // ⬅️ тот же цвет
            tonalElevation = 0.dp,    // ⬅️ не тонируем повторно (иначе оттенок изменится)
            shadowElevation = elevation
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Meow-ment of truth\nSet your Custom Timer and Bell Sound in Settings",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Thanks, I know this now")
                }
            }
        }
    }
}

@Composable
fun PlainIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    IconButton(onClick = onClick) { icon() }
}

@Composable
private fun MenuWithContent(
    viewState: BreakActivityViewState,
    showDialog: MutableState<Boolean>,
    viewModel: BreakActivityViewModel
) {
    when (val screenContent = viewState.screenContent) {
        is ScreenContent.AudioContent -> Audio(
            screenContent.audio,
            showDialog.value,
            onWatched30Seconds = { viewModel.onWatchedVideo(screenContent.audio) },
            onVideoEnded = { viewModel.onVideoEnded(true) }
        )

        is ScreenContent.Menu -> HierarchicalMenu(
            currentMenu = screenContent.currentMenu,
            onClick = { viewModel.navigateTo(it) },
            onRandomWorkoutClick = { viewModel.onRandomWorkoutClick() }
        )

        is ScreenContent.VideoContent -> Video(
            screenContent.video,
            showDialog.value,
            onWatched30Seconds = { viewModel.onWatchedVideo(screenContent.video) },
            onVideoEnded = { viewModel.onVideoEnded(true) }
        )

        is ScreenContent.GoForIt -> GoForIt(screenContent.drawableResource)
    }
}

@Composable
fun KittyWithLaptop() {
    var showDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val size = minOf(maxWidth, maxHeight) * 0.8f

        // Заголовок с "жирным" эффектом через дублирование
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.TopCenter)
                .offset(x = -maxWidth * 0.03f)
        ) {
            // псевдо-обводка только по X
            listOf(-1, 1).forEach { dx ->
                Text(
                    text = "Focus, Kitty!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp // 🔹 расстояние между буквами
                    ),
                    modifier = Modifier.offset(x = dx.dp, y = 0.dp)
                )
            }
            // основной текст
            Text(
                text = "Focus, Kitty!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            )
        }

        Image(
            painter = painterResource(Res.drawable.cat_laptop),
            contentDescription = "Kitty with laptop",
            modifier = Modifier
                .align(Alignment.Center)
                .size(size),
            contentScale = ContentScale.Fit
        )

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Text("Send Feedback")
        }

        if (showDialog) {
            FeedbackDialog(
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun Title(
    text: String,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    currentTime: String,
) {
    val windowInfo = rememberWindowInfo()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (windowInfo.isSmallScreen()) 48.dp else 64.dp)
    ) {
        if (showBackButton) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            if (windowInfo.isSmallScreen()) {
                Text(
                    text = currentTime,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Slightly less prominent
                )
            }

        }

    }
}

@Composable
fun ToggleBreakTypeButton(
    selectedBreakType: TimerState,
    isShortBreakSelected: (Boolean) -> Unit,
) {
    var isLongSelected by remember(selectedBreakType) {
        mutableStateOf(
            when (selectedBreakType) {
                TimerState.LongBreak -> true
                is TimerState.Pomodoro -> false
                is TimerState.ShortBreak -> false
            }
        )
    }

    val buttonText = if (isLongSelected) "Short break" else "Long break"

    Button(
        onClick = {
            isLongSelected = !isLongSelected
            if (isLongSelected) {
                isShortBreakSelected(false)
            } else {
                isShortBreakSelected(true)
            }
        },
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
        ),
    ) {
        Text(
            text = buttonText,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun Audio(
    video: Video,
    showDialog: Boolean,
    onWatched30Seconds: () -> Unit,
    onVideoEnded: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 16.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Show those moves! Dance like nobody watching",
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        if (!showDialog) {
            VideoPlayer(
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 8.dp
                ).fillMaxWidth()
                    .aspectRatio(16f / 9f),
                url = video.url,
                onWatched70Percent = onWatched30Seconds,
                onVideoEnded = onVideoEnded
            )
        }
    }
}

@Composable
fun Video(
    video: Video,
    showDialog: Boolean,
    onWatched30Seconds: () -> Unit,
    onVideoEnded: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showDialog) {
            VideoPlayer(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                url = video.url,
                onWatched70Percent = onWatched30Seconds,
                onVideoEnded = onVideoEnded
            )
        }
    }
}

@Composable
fun GoForIt(drawableResource: DrawableResource? = null) {
    if (drawableResource != null) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val imageSize = minOf(maxWidth, maxHeight * 0.62f)
            val topWeight = 0.50f
            val bottomWeight = 1f - topWeight

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(topWeight))

                Image(
                    painter = painterResource(drawableResource),
                    contentDescription = null,
                    modifier = Modifier.size(imageSize).offset(x = 24.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(0.dp))
                Text(
                    text = "Go for it!",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        lineHeight = MaterialTheme.typography.headlineMedium.fontSize,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                    modifier = Modifier.offset(y = (-20).dp)
                )

                Spacer(Modifier.weight(bottomWeight))
            }
        }
    } else {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.headlineMedium,
            text = "Coming soon…",
        )
    }
}


@Composable
fun HierarchicalMenu(
    currentMenu: CurrentMenu,
    modifier: Modifier = Modifier, // Keep this for external layout control if needed
    onClick: (MenuItem) -> Unit,
    onRandomWorkoutClick: () -> Unit
) {
    val cornerRadius = 12.dp
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 8.dp, end = 8.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = currentMenu.title != null,
            content = {
                currentMenu.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    )
                }
            }
        )

        when (currentMenu.type) {
            Type.LIST -> ListMenu(currentMenu, onClick)
            Type.GRID -> GridMenu(
                currentMenu = currentMenu,
                onClick = onClick,
                modifier = Modifier.padding(top = if (currentMenu.title != null) 0.dp else 8.dp),
                onRandomWorkoutClick = onRandomWorkoutClick,
            )

            Type.AUDIO -> AudioMenu(currentMenu, onClick)
        }
    }
}

@Composable
fun AudioMenu(
    currentMenu: CurrentMenu,
    onClick: (MenuItem) -> Unit
) {
    currentMenu.menuItems.forEach { item ->
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            onClick = {
                onClick(item)
            }
        ) {
            ListItem(
                headlineContent = {
                    WithPlatformEmoji(item.title) { text, inlineContent ->
                        Text(
                            text = text,
                            textAlign = TextAlign.Start, // Align to start for better layout with leading icon
                            style = MaterialTheme.typography.titleMedium,
                            inlineContent = inlineContent
                        )
                    }
                },
                supportingContent = item.subtitle?.let { subtitleText ->
                    {
                        Text(
                            text = subtitleText,
                            textAlign = TextAlign.Start, // Align to start
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                        )
                    }
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow, // Dummy icon for audio
                        contentDescription = "Audio track", // Decorative, or provide specific description
                        modifier = Modifier.size(40.dp), // Adjust size as needed
                        tint = MaterialTheme.colorScheme.primary // Example tint
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp // Slightly more vertical padding for items with icons
                ),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer, // Match card
                    headlineColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    leadingIconColor = MaterialTheme.colorScheme.primary // Ensure icon color is set if needed
                )
            )
        }
    }
}

@Composable
fun ListMenu(
    currentMenu: CurrentMenu,
    onClick: (MenuItem) -> Unit
) {
    currentMenu.menuItems.forEach { item ->
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            onClick = {
                onClick(item)
            }
        ) {
            ListItem(
                headlineContent = {
                    WithPlatformEmoji(item.title) { text, inlineContent ->
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = text,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium, // Example style
                            inlineContent = inlineContent
                        )
                    }


                },
                supportingContent = item.subtitle?.let { subtitleText ->
                    {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = subtitleText,
                            textAlign = TextAlign.Center, // Or another alignment if you prefer
                            style = MaterialTheme.typography.bodyMedium, // Example style
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f) // Slightly less prominent
                        )
                    }
                },
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    headlineColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    supportingColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
fun GridMenu(
    currentMenu: CurrentMenu,
    onClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier,
    onRandomWorkoutClick: () -> Unit
) {
    val numberOfColums = 2
    val columns = GridCells.Fixed(numberOfColums) // Or GridCells.Adaptive for responsiveness

    LazyVerticalGrid(
        columns = columns,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .heightIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(
            span = {
                GridItemSpan(numberOfColums)
            }
        ) {
            if (currentMenu.menuItems.isNotEmpty()) {
                Button(
                    onClick = onRandomWorkoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Gimme random")
                }
            }
        }
        if (currentMenu.menuItems.isEmpty()) {
            item(
                span = { GridItemSpan(numberOfColums) }
            ) {
                Text(
                    text = "There are no videos yet,\ncheck other categories.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(currentMenu.menuItems, key = { it.id }) { menuItem ->
                GridItem(
                    menuItem = menuItem,
                    onClick = { onClick(menuItem) }
                )
            }
        }

    }
}


@Composable
private fun GridItem(
    menuItem: MenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                menuItem.imageUrl?.let {
                    NetworkImage(
                        url = it,
                        contentDescription = menuItem.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            HorizontalDivider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = menuItem.title,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
