package org.timer.main

import ToastHost
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import org.koin.compose.viewmodel.*
import org.timer.main.auth.*
import org.timer.main.breakactivity.*
import org.timer.main.progress.header.*
import org.timer.main.projects.*
import org.timer.main.statistic.*
import org.timer.main.team.activity.*
import org.timer.main.timer.*

@ExperimentalMaterial3Api
@Composable
fun WebMainScreen(
    authViewModel: AuthViewModel = koinViewModel()
) {
    val viewState by authViewModel.viewState.collectAsStateWithLifecycle()
    if (viewState.isLoading) {
        FullScreenLoadingIndicator()
    } else {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = if (viewState.isLoggedIn) WebRouts.Main.destanation else WebRouts.Login.destanation,
        ) {
            composable(WebRouts.Login.destanation) {
                LoginScreen(viewModel = authViewModel)
            }
            composable(WebRouts.Main.destanation) {
                MainScreen(
                    navigateToKittyScreen = { navController.navigate(WebRouts.Kitty.destanation) },
                    navigateToTeamActivityScreen = { navController.navigate(WebRouts.TeamActivity.destanation) })
            }
            composable(WebRouts.Kitty.destanation) {
                KittyScreen(onBackPress = { navController.popBackStack() })
            }
            composable(WebRouts.TeamActivity.destanation) {
                TeamActivityScreen(onBackPress = { navController.popBackStack() })
            }
        }

        LaunchedEffect(viewState.isLoggedIn) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (viewState.isLoggedIn) {
                if (currentRoute != WebRouts.Main.destanation) {
                    navController.navigate(WebRouts.Main.destanation) {
                        popUpTo(navController.graph.findStartDestination().route.toString()) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                if (currentRoute != WebRouts.Login.destanation) {
                    navController.navigate(WebRouts.Login.destanation) {
                        popUpTo(navController.graph.findStartDestination().route.toString()) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}


@ExperimentalMaterial3Api
@Composable
private fun MainScreen(
    viewModel: TimerViewModel = koinViewModel(),
    navigateToKittyScreen: () -> Unit = {},
    navigateToTeamActivityScreen: () -> Unit = {},
    catBowlProgressViewModel: CatBowlProgressViewModel = koinViewModel()
) {
    val toastHostState = rememberToastHostState()

    LaunchedEffect(Unit) {
        catBowlProgressViewModel.toastMessages.collect { msg ->
            toastHostState.showToast(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

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
                ),

                ) {
                Column {
                    TimerScreen(
                        modifier = Modifier
                            .fillMaxHeight(0.5f)
                            .fillMaxWidth(),
                        viewModel, navigateToStatisticScreen = navigateToKittyScreen
                    )
                    Row { HorizontalDivider(thickness = 2.dp) }
                    ProjectsScreen(
                        modifier = Modifier
                            .fillMaxHeight(1f)
                            .fillMaxWidth(),
                        timerViewModel = viewModel, navigateToKittyScreen = navigateToKittyScreen
                    )
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
                BreakActivityScreen(
                    navigateToStatisticScreen = navigateToKittyScreen,
                    navigateToTeamActivityScreen = navigateToTeamActivityScreen
                )
            }
        }
        ToastHost(toastHostState)
    }
}

@Composable
fun FullScreenLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator()
    }
}

private sealed class WebRouts(
    val destanation: String,
) {
    data object Main : WebRouts("Main")
    data object Login : WebRouts("Login")
    data object Kitty : WebRouts("Kitty")
    data object TeamActivity : WebRouts("TeamActivity")

}