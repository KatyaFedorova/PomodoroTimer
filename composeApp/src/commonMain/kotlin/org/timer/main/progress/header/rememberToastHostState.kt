package org.timer.main.progress.header

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.*

@Composable
fun rememberToastHostState(): ToastHostState {
    return remember { ToastHostState() }
}

class ToastHostState {
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    suspend fun showToast(message: String) {
        _messages.emit(message)
    }
}
