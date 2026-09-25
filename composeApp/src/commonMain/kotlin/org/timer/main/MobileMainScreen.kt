package org.timer.main

import ToastHost
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import org.jetbrains.compose.resources.*
import org.koin.compose.viewmodel.*
import org.timer.main.auth.*
import org.timer.main.breakactivity.*
import org.timer.main.progress.header.*
import org.timer.main.settings.*
import org.timer.main.statistic.*
import org.timer.main.team.activity.*
import org.timer.main.timer.*
import org.timer.ui.theme.*
import pomodorotimer.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileMainScreen(
    authViewModel: AuthViewModel = koinViewModel(),
) {
    val authViewState by authViewModel.viewState.collectAsStateWithLifecycle()
    if (authViewState.isLoading) {
        FullScreenLoadingIndicator()
    } else {
        MainContent(authViewState = authViewState, authViewModel = authViewModel)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainContent(
    timerViewModel: TimerViewModel = koinViewModel(),
    authViewState: AuthViewState,
    authViewModel: AuthViewModel,
    catBowlProgressViewModel: CatBowlProgressViewModel = koinViewModel()
) {
    val toastHostState = rememberToastHostState()

    LaunchedEffect(Unit) {
        catBowlProgressViewModel.toastMessages.collect { msg ->
            toastHostState.showToast(msg)
        }
    }

    val navController = rememberNavController()
    val viewState by timerViewModel.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(viewState.navigateToActivitiesScreen) {
        if (viewState.navigateToActivitiesScreen) {
            timerViewModel.onNavigatedToActivitiesScreen()
            navigateToScreen(navController, MobileRouts.Activities)
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(
                navController = navController,
                startDestination = if (authViewState.isLoggedIn) MobileRouts.Home.destanation else MobileRouts.Login.destanation,
            ) {
                composable(MobileRouts.Home.destanation) {
                    TimerScreen(viewModel = timerViewModel, navigateToStatisticScreen = {
                        navigateToScreen(navController, MobileRouts.Kitty)
                    })
                }
                composable(MobileRouts.Activities.destanation) {
                    BreakActivityScreen(
                        timerViewModel,
                        navigateToStatisticScreen = {
                            navigateToScreen(navController, MobileRouts.Kitty)
                        },
                        navigateToTimerScreen = {
                            navigateToScreen(navController, MobileRouts.Home)
                        }
                    )
                }
                composable(MobileRouts.Settings.destanation) {
                    SettingsDialogScreen(isDialogVisible = {})
                }
                composable(MobileRouts.Login.destanation) {
                    LoginScreen(
                        Modifier.background(MaterialTheme.colorScheme.background),
                        authViewModel
                    )
                }
                composable(MobileRouts.Kitty.destanation) {
                    KittyScreen(onBackPress = { navController.popBackStack() })
                }
                composable(MobileRouts.Statistic.destanation) {
                    PlotContent()
                }
                composable(MobileRouts.TeamActivity.destanation) {
                    TeamActivityScreen(onBackPress = { navController.popBackStack() })
                }
            }
            ToastHost(toastHostState) // 👈 наш кастомный "toast"
        }
    }

    LaunchedEffect(authViewState.isLoggedIn) {
        val currentRoute = navController.getCurrentRoute()
        if (authViewState.isLoggedIn) {
            if (currentRoute != MobileRouts.Home.destanation) {
                navController.navigate(MobileRouts.Home.destanation) {
                    popUpTo(navController.graph.findStartDestination().route.toString()) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        } else {
            if (currentRoute != MobileRouts.Login.destanation) {
                navController.navigate(MobileRouts.Login.destanation) {
                    popUpTo(navController.graph.findStartDestination().route.toString()) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
}

private fun NavHostController.getCurrentRoute() = currentBackStackEntry?.destination?.route

@Composable
fun BottomBar(navController: NavHostController) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkScheme else lightScheme) {
        val screens = listOf(
            MobileRouts.Home,
            MobileRouts.Activities,
            MobileRouts.Kitty,
            MobileRouts.TeamActivity,
            MobileRouts.Statistic,
            MobileRouts.Settings,
        )
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination?.route ?: ""

        val bottomBarDestination = screens.any { it.destanation == currentDestination }
        if (bottomBarDestination) {
            NavigationBar {
                screens.forEach { screen ->
                    AddItem(
                        screen = screen,
                        currentDestination = currentDestination,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: MobileRouts,
    currentDestination: String,
    navController: NavHostController
) {
    NavigationBarItem(
        icon = {
            screen.icon?.let {
                Icon(
                    imageVector = vectorResource(it),
                    contentDescription = null
                )
            }
        },
        label = {
            screen.resourceId?.let {
                Text(
                    it,
                    maxLines = 1,
                    style = LocalTextStyle.current.copy(fontSize = 10.sp)
                )
            }
        },
        selected = currentDestination == screen.destanation,
        onClick = {
            if (currentDestination == screen.destanation) {
                return@NavigationBarItem
            }
            navigateToScreen(navController, screen)
        }
    )
}

private fun navigateToScreen(
    navController: NavHostController,
    screen: MobileRouts
) {
    navController.navigate(screen.destanation, {
        popUpTo(navController.graph.findStartDestination().route.toString()) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    })
}

sealed class MobileRouts(
    val destanation: String,
    val resourceId: String? = null,
    val icon: DrawableResource? = null
) {
    data object Home : MobileRouts("timer", "Timer", Res.drawable.home)
    data object Activities : MobileRouts("activity", "Activities", Res.drawable.run)
    data object Settings : MobileRouts("settings", "Settings", Res.drawable.settings)
    data object Login : MobileRouts("Login")
    data object Kitty : MobileRouts("Kitty", "Kitty", Res.drawable.kitty)
    data object Statistic : MobileRouts("Statistic", "Statistic", Res.drawable.kitty)
    data object TeamActivity : MobileRouts("TeamActivity", "Team", Res.drawable.team)

}