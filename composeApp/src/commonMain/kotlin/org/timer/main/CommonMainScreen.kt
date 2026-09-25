package org.timer.main


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

@ExperimentalMaterial3Api
@Composable
fun CommonMainScreen(
    windowInfo: WindowInfo = rememberWindowInfo()
) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        if (windowInfo.isSmallScreen()) {
            MobileMainScreen()
        } else {
            WebMainScreen()
        }
    }
}












