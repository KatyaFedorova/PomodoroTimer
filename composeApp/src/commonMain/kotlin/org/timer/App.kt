package org.timer

import androidx.compose.material3.*
import androidx.compose.runtime.*
import org.jetbrains.compose.resources.*
import org.jetbrains.compose.ui.tooling.preview.*
import org.kodein.emoji.compose.*
import org.koin.compose.*
import org.timer.main.*
import org.timer.ui.theme.*

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App() {
    remember { EmojiService.initialize() }
    if (isWeb() || isIOS()) {
        KoinApplication(application = { modules(appModule(), platformSpecificModule()) }) {
            MainContent()
        }
    } else { //android
        KoinContext {
            MainContent()
        }
    }

    LaunchedEffect(Unit) {
        afterKoinStarted()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent() {
    MaterialTheme(lightScheme) {
        CommonMainScreen()
    }
}

