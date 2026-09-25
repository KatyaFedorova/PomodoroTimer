package org.timer.main.timer

import androidx.compose.runtime.*
import dev.icerock.moko.permissions.*
import dev.icerock.moko.permissions.compose.*
import dev.icerock.moko.permissions.notifications.*
import kotlinx.coroutines.*

@Composable
actual fun AskNotificationPermission() {
    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController =
        remember(factory) { factory.createPermissionsController() }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        coroutineScope.launch {

            if (!controller.isPermissionGranted(Permission.REMOTE_NOTIFICATION)) {
                try {
                    controller.providePermission(Permission.REMOTE_NOTIFICATION)
                } catch (deniedException: DeniedException) {
                    println("Permission denied")
                } catch (deniedAlwaysException: DeniedAlwaysException) {
                    println("Permission denied permenantly")
                } catch (exception: Exception) {
                    println("Notification permission failed" + exception)
                }
            }
        }
    }
}