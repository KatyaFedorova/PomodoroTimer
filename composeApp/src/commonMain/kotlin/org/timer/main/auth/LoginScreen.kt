package org.timer.main.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    if (viewState.showMagicLinkSentDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDialog,
            confirmButton = {
                Button(onClick = viewModel::onDismissDialog) {
                    Text("OK")
                }
            },
            title = { Text("Magic Link Sent") },
            text = {
                Text("We’ve sent a magic sign-in link to ${viewState.email}.\n" +
                        "If you don’t see it in your inbox, please check your spam folder.")
            },
        )
    }

    LaunchedEffect(viewState.errorMessage) {
        viewState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please sign in to continue.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.onLoginClick() },
                ) {
                    Text(text = "Sign in with Google")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(100.dp)
                            .padding(end = 16.dp)
                    )
                    Text(
                        text = "or",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .width(100.dp)
                            .padding(start = 16.dp)
                    )
                }

                Text(
                    text = "Sign in with Email",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    modifier = Modifier.width(300.dp),
                    value = viewState.email,
                    onValueChange = { viewModel.onEmailUpdate(it) },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.onSendMagicLink(viewState.email) }
                    ),
                    isError = viewState.showEmailError,
                    supportingText = {
                        Box(Modifier.height(20.dp)) {
                            if (viewState.showEmailError) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Invalid email",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    enabled = viewState.isSubmitEnabled,
                    onClick = { viewModel.onSendMagicLink(viewState.email) }
                ) {
                    Text("Send Magic Link")
                }
            }

            if (viewState.loginScreenIsLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
