package org.timer.main.projects

import SwipeToDeleteContainer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import org.koin.compose.viewmodel.*
import org.timer.main.*
import org.timer.main.WindowInfo
import org.timer.main.domain.project.*
import org.timer.main.progress.header.*
import org.timer.main.timer.*
import org.timer.main.timer.TimerViewState
import sh.calvin.reorderable.*


val COLLAPSED_TOP_BAR_HEIGHT = 62.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = koinViewModel(),
    timerViewModel: TimerViewModel,
    windowInfo: WindowInfo = rememberWindowInfo(),
    navigateToKittyScreen: () -> Unit
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val timerViewState by timerViewModel.viewState.collectAsStateWithLifecycle()
    //comment 2
    var showDialog by remember { mutableStateOf(false) }
    var prevSize by remember { mutableStateOf(viewState.projects.size) }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(viewState.projects.size) {
        val currentSize = viewState.projects.size
        if (prevSize < currentSize && currentSize > 0) {
            lazyListState.animateScrollToItem(currentSize - 1)
        }
        prevSize = currentSize
    }

    viewState.projects.forEachIndexed { projectIndex, project ->
        LaunchedEffect(key1 = project.id, key2 = project.tasks.size) {
            if (lazyListState.layoutInfo.totalItemsCount > 0 &&
                lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == projectIndex &&
                project.tasks.isNotEmpty()
            ) {
                lazyListState.animateScrollBy(200f)
            }
        }
    }

    val headersNumber = if (windowInfo.isSmallScreen()) 1 else 0
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            viewModel.onProjectsDrugAndDrop(from.index - headersNumber, to.index - headersNumber)
        }

    )

    val density = LocalDensity.current
    val isToolbarCollapsed: Boolean by remember {
        val scrollOffsetThresholdInPx = with(density) { 150.dp.toPx() }
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > scrollOffsetThresholdInPx
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onProjectTitleUpdate("")
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, "Add Project")
            }
        },
        topBar = {
            if (windowInfo.isSmallScreen()) {
                CollapsedTopBar(
                    isCollapsed = isToolbarCollapsed,
                    viewState = timerViewState,
                    viewModel = timerViewModel,
                    lazyListState = lazyListState,
                    navigateToStatistickScreen = navigateToKittyScreen
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewState.projects.isEmpty()) {
                Column(
                    modifier = Modifier.padding(
                        top = 8.dp,
                        start = 16.dp,
                        bottom = 16.dp,
                        end = 16.dp
                    )
                ) {
                    if (windowInfo.isSmallScreen()) {
                        TimerPager(timerViewModel, timerViewState)
                        Spacer(modifier = Modifier.size(8.dp))
                        ProjectsTitle()
                    }
                    EmptyState()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 8.dp
                    ),
                    state = lazyListState,
                ) {

                    if (windowInfo.isSmallScreen()) {
                        item {
                            TimerPager(timerViewModel, timerViewState)
                            Spacer(modifier = Modifier.size(8.dp))
                            ProjectsTitle()
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                    }
                    itemsIndexed(
                        viewState.projects,
                        key = { _, item -> item.id }
                    ) { _, item ->
                        Spacer(modifier = Modifier.size(8.dp))
                        ReorderableItem(reorderState, key = item.id) { isDragging ->
                            var showDeleteDialog by remember { mutableStateOf(false) }

                            SwipeToDeleteContainer(
                                onDismissed = { showDeleteDialog = true },
                                shape = CardDefaults.shape,
                                freezeSwipe = showDeleteDialog,
                            ) {
                                ShortLongPress {
                                    ProjectItem(
                                        modifier = Modifier.animateItem(),
                                        project = item,
                                        isDrugging = isDragging,
                                        headerModifier = Modifier.longPressDraggableHandle(), // 👈 handle передаём сюда
                                        onDeleteClick = { showDeleteDialog = true },
                                        onSubmitTaskClick = { project, description ->
                                            viewModel.onTaskSubmitClick(project.id, description)
                                        },
                                        onTaskDoneClick = { project, task ->
                                            viewModel.onTaskDoneClick(project.id, task.id)
                                        },
                                        onTaskUndoneClick = { project, task ->
                                            viewModel.onTaskUndoneClick(project.id, task.id)
                                        },
                                        onTasksReorder = { project, fromIndex, toIndex ->
                                            viewModel.onTasksDrugAndDrop(
                                                project.id,
                                                fromIndex,
                                                toIndex
                                            )
                                        },
                                        onSubmitEditProjectName = { id, name ->
                                            viewModel.onSubmitEditProjectName(id, name)
                                        },
                                        onSubmitEditTaskDescription = { projectId, taskId, name ->
                                            viewModel.onSubmitEditTaskDescription(
                                                projectId,
                                                taskId,
                                                name
                                            )
                                        },
                                        onDeleteTaskClick = { projectId, taskId ->
                                            viewModel.onDeleteTaskClick(projectId, taskId)
                                        },
                                        onDeleteDoneTasksClick = { projectId ->
                                            viewModel.onDeleteDoneTasksClick(projectId)
                                        }
                                    )
                                }
                            }




                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        showDeleteDialog = false
                                    },
                                    title = { Text("Confirm deletion") },
                                    text = { Text("Are you sure you want to delete this project?") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showDeleteDialog = false
                                                viewModel.removeProject(item.id)
                                            }
                                        ) {
                                            Text("Yes")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                showDeleteDialog = false
                                            }
                                        ) {
                                            Text("No")
                                        }
                                    }
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    if (showDialog) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        AlertDialog(
            title = { Text(text = "Add Project") },
            text = {
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = viewState.projectName,
                    onValueChange = { viewModel.onProjectTitleUpdate(it) },
                    label = { Text("Name") }
                )
            },
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        viewModel.onAddProjectSubmitClick()
                    },
                    enabled = viewState.isSubmitEnabled
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
private fun CollapsedTopBar(
    modifier: Modifier = Modifier,
    isCollapsed: Boolean,
    viewState: TimerViewState,
    viewModel: TimerViewModel,
    lazyListState: LazyListState,
    navigateToStatistickScreen: () -> Unit
) {

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(COLLAPSED_TOP_BAR_HEIGHT),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedVisibility(
                visible = !isCollapsed,
                modifier = Modifier.align(Alignment.Center)
            ) {
                CatBowlProgressHeader(navigateToStatisticScreen = navigateToStatistickScreen)
            }

            AnimatedVisibility(visible = isCollapsed) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // Pushes items to ends
                ) {
                    // Timer Type and Time (takes available space, pushing button to the end)
                    val title = when (viewState.selectedTabIndex) {
                        0 -> "Kittydoro ${viewState.pomodoroTime}"
                        1 -> "Short Break ${viewState.shortBreakTime}"
                        2 -> "Long Break ${viewState.longBreakTime}"
                        else -> throw IllegalArgumentException()
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp)) // Space between text and button

                    when (viewState.selectedTabIndex) {
                        0 -> KittydoroStartResetButton(viewState, viewModel, lazyListState)
                        1 -> KittydoroStartResetButton(viewState, viewModel, lazyListState)
                        2 -> KittydoroStartResetButton(viewState, viewModel, lazyListState)
                    }

                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center),
            text = "Your project list is empty.\n Add your first project using button below.",
        )
    }
}

@Composable
fun ProjectItem(
    modifier: Modifier,
    project: PresentableProject,
    isDrugging: Boolean,
    onDeleteClick: (String) -> Unit,
    onSubmitTaskClick: (PresentableProject, String) -> Unit,
    onTaskDoneClick: (PresentableProject, Task) -> Unit,
    onTaskUndoneClick: (PresentableProject, Task) -> Unit,
    onTasksReorder: (PresentableProject, Int, Int) -> Unit,
    onSubmitEditProjectName: (String, String) -> Unit,
    onSubmitEditTaskDescription: (String, Long, String) -> Unit,
    onDeleteTaskClick: (String, Long) -> Unit,
    onDeleteDoneTasksClick: (String) -> Unit,
    headerModifier: Modifier = Modifier, // 👈 сюда попадает handle
) {
    val elevation by animateDpAsState(if (isDrugging) 6.dp else 2.dp)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(
                elevation = elevation,
                shape = CardDefaults.shape,
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            }
    ) {
        val lazyListState = rememberLazyListState()
        val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
            onTasksReorder.invoke(project, from.index, to.index)
        }
        var isAddTaskClicked by remember { mutableStateOf(false) }
        Column {
            Header(
                modifier = headerModifier, // 👈 вот здесь
                project = project,
                onDeleteProjectClick = { onDeleteClick(project.id) },
                onAddTaskClick = {
                    isAddTaskClicked = true
                },
                onSubmitEditProjectName = onSubmitEditProjectName,
                onDeleteDoneTasksClick = { onDeleteDoneTasksClick(project.id) }
            )
        }
        Column(modifier = Modifier.padding(start = 16.dp,end = 16.dp, bottom = 16.dp)) {
            HorizontalDivider()
            LazyColumn(
                modifier = Modifier
                    .wrapContentHeight()
                    .heightIn(max = 5000.dp),
                state = lazyListState,
            ) {
                itemsIndexed(project.tasks, key = { _, item -> item.id }) { index, item ->
                    ReorderableItem(reorderState, key = item.id) { isDragging ->
                        SwipeToDeleteContainer(
                            onDismissed = { onDeleteTaskClick(project.id, item.id) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                ShortLongPress {
                                    TaskItem(
                                        modifier = Modifier.animateItem()
                                            .longPressDraggableHandle(),
                                        task = item,
                                        isDrugging = isDragging,
                                        onTaskDoneClick = { onTaskDoneClick.invoke(project, it) },
                                        onTaskUndoneClick = {
                                            onTaskUndoneClick.invoke(
                                                project,
                                                it
                                            )
                                        },
                                        onSubmitEditTaskDescription = { taskId, desc ->
                                            onSubmitEditTaskDescription(project.id, taskId, desc)
                                        },
                                        onDeleteTaskClick = { taskId ->
                                            onDeleteTaskClick(project.id, taskId)
                                        }
                                    )
                                }
                            }
                        }


                    }
                }
            }

            val addTaskFocusRequester = remember { FocusRequester() }

            if (project.showAddTaskFooter || isAddTaskClicked) {
                Column {
                    HorizontalDivider()
                    AddTaskFooter(
                        onSubmitTaskClick = { taskName ->
                            isAddTaskClicked = false
                            onSubmitTaskClick(project, taskName)
                        },
                        addTaskFocusRequester
                    )
                }
            }

            LaunchedEffect(isAddTaskClicked) {
                if (isAddTaskClicked) {
                    addTaskFocusRequester.requestFocus()
                }
            }
        }
    }
}


@Composable
fun AddTaskFooter(
    onSubmitTaskClick: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    var taskName by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Add Task") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
                .focusRequester(focusRequester),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (taskName.isNotBlank()) {
                        onSubmitTaskClick(taskName)
                    }
                }
            ),
        )
        Button(
            modifier = Modifier.padding(top = 8.dp),
            onClick = {
                if (taskName.isNotBlank()) {
                    onSubmitTaskClick(taskName)
                }
            },
            enabled = taskName.isNotBlank()
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun TaskItem(
    modifier: Modifier = Modifier,
    task: Task,
    isDrugging: Boolean,
    onTaskDoneClick: (Task) -> Unit,
    onTaskUndoneClick: (Task) -> Unit,
    onSubmitEditTaskDescription: (Long, String) -> Unit,
    onDeleteTaskClick: (Long) -> Unit,
    windowInfo: WindowInfo = rememberWindowInfo(), // 👈 твой windowInfo
) {
    var isEditing by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    var displayedText by remember { mutableStateOf(task.description) }
    var currentEditValue by remember(task.description) {
        mutableStateOf(TextFieldValue(task.description, TextRange(task.description.length)))
    }

    LaunchedEffect(task.description) {
        if (!isEditing) {
            if (displayedText != task.description) {
                displayedText = task.description
            }
            if (currentEditValue.text != task.description) {
                currentEditValue =
                    TextFieldValue(task.description, TextRange(task.description.length))
            }
        }
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            if (currentEditValue.text != displayedText ||
                currentEditValue.selection.end != displayedText.length
            ) {
                currentEditValue =
                    TextFieldValue(displayedText, TextRange(displayedText.length))
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    var isFocusLostByDoneAction by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val elevation by animateDpAsState(if (isDrugging) 2.dp else 0.dp)

    LaunchedEffect(isEditing) {
        if (isEditing) {
            isFocusLostByDoneAction = false
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column(modifier = modifier.shadow(elevation = elevation)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .alpha(if (task.isDone) 0.5f else 1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        onTaskDoneClick(task)
                    } else {
                        onTaskUndoneClick(task)
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (isEditing) {
                    var localHasFocus by remember { mutableStateOf(false) }
                    BasicTextField(
                        value = currentEditValue,
                        onValueChange = { currentEditValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    localHasFocus = true
                                } else {
                                    if (localHasFocus && !isFocusLostByDoneAction) {
                                        val newText = currentEditValue.text
                                        if (newText != task.description) {
                                            displayedText = newText
                                            onSubmitEditTaskDescription(task.id, newText)
                                        } else {
                                            displayedText = task.description
                                        }
                                        isEditing = false
                                    }
                                    localHasFocus = false
                                }
                            }
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        textStyle = if (task.isDone)
                            LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough)
                        else LocalTextStyle.current,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val newText = currentEditValue.text
                                isFocusLostByDoneAction = true
                                if (newText != task.description) {
                                    displayedText = newText
                                    onSubmitEditTaskDescription(task.id, newText)
                                } else {
                                    displayedText = task.description
                                }
                                isEditing = false
                                keyboardController?.hide()
                            }
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    )
                } else {
                    Text(
                        text = displayedText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEditing = true }
                            .padding(start = 4.dp),
                        style = if (task.isDone)
                            LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough)
                        else LocalTextStyle.current,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!windowInfo.isSmallScreen()) { // 👈 скрываем More на маленьких экранах
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    IconButton(
                        modifier = Modifier.size(20.dp),
                        onClick = { isMenuExpanded = true }
                    ) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDeleteTaskClick(task.id)
                                isMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    project: PresentableProject,
    onDeleteProjectClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onSubmitEditProjectName: (String, String) -> Unit,
    onDeleteDoneTasksClick: () -> Unit,
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(project.name, TextRange(project.name.length)))
    }
    val focusRequester = remember { FocusRequester() }
    var isEditing by remember { mutableStateOf(false) }
    var shouldSubmit by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val windowInfo: WindowInfo = rememberWindowInfo()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Общий стиль текста для обоих состояний
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        fontSize = 20.sp // увеличенный размер шрифта
    )
    Box(modifier.fillMaxSize()){
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp), // 👈 теперь header = drag handle
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 32.dp, end = 16.dp, bottom = 8.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .background(Color.Transparent)
                            .onFocusChanged { focusState ->
                                if (isFocused != focusState.isFocused) {
                                    isFocused = focusState.isFocused
                                    if (!focusState.isFocused && shouldSubmit) {
                                        onSubmitEditProjectName(project.id, textFieldValue.text)
                                        isEditing = false
                                    }
                                }
                                shouldSubmit = true
                            },
                        textStyle = textStyle,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onSubmitEditProjectName(project.id, textFieldValue.text)
                                isEditing = false
                                keyboardController?.hide()
                            }
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = VisualTransformation.None
                    )
                }
                LaunchedEffect(isEditing) {
                    if (isEditing) focusRequester.requestFocus()
                }
            } else {
                Text(
                    text = project.name,
                    modifier = Modifier
                        .padding(start = 32.dp, end = 16.dp, top = 0.dp, bottom = 8.dp)
                        .weight(1f)
                        .clickable {
                            isEditing = true
                            textFieldValue =
                                TextFieldValue(project.name, TextRange(project.name.length))
                        },
                    style = textStyle,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }

            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clip(CircleShape),
                    onClick = onAddTaskClick
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add", modifier = Modifier.size(24.dp))
                }

                Box {
                    IconButton(
                        modifier = Modifier.size(37.dp),
                        onClick = { isMenuExpanded = true }
                    ) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        if (!windowInfo.isSmallScreen()) {
                            DropdownMenuItem(
                                text = { Text("Delete project") },
                                onClick = {
                                    onDeleteProjectClick()
                                    isMenuExpanded = false
                                }
                            )
                            HorizontalDivider()
                        }
                        DropdownMenuItem(
                            text = { Text("Delete ticked tasks") },
                            onClick = {
                                onDeleteDoneTasksClick()
                                isMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }

}


@Composable
fun ProjectsTitle() {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "Projects",
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary
    )
}

