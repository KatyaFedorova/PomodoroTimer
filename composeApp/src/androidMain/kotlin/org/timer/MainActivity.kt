package org.timer

import android.app.*
import android.content.*
import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.*
import org.koin.androidx.viewmodel.ext.android.*
import org.timer.main.*
import org.timer.main.auth.*

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAuthBridge.init(this)
        setContent {
            App()
        }
        createNotificationChannel()
        handleMagickLink()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ForegroundServiceChannelId",
                "Live Activity Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleMagickLink()
    }

    private fun handleMagickLink() {
        intent.data?.let { data ->
            authViewModel.onMagickLinkClick(data.toString())
        }
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}