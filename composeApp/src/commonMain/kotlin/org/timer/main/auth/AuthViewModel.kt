package org.timer.main.auth

import androidx.lifecycle.*
import com.diamondedge.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.timer.*
import org.timer.main.domain.auth.*

class AuthViewModel(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val sendMagicLinkUseCase: SendMagicLinkUseCase,
    private val loginWithMagicLinkUseCase: LoginWithMagicLinkUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase
) : ViewModel() {
    private val _viewState = MutableStateFlow(AuthViewState())
    val viewState: StateFlow<AuthViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            getAuthStateUseCase().collect { authState ->
                when (authState) {
                    is AuthState.Authenticated ->
                        _viewState.update { it.copy(isLoggedIn = true, isLoading = false) }

                    AuthState.Loading ->
                        _viewState.update { it.copy(isLoading = true) }

                    AuthState.NotAuthenticated ->
                        _viewState.update { it.copy(isLoggedIn = false, isLoading = false) }

                }
            }
        }
    }

    fun onLoginClick() = viewModelScope.launch {
        _viewState.update { it.copy(loginScreenIsLoading = true) }
        try {
            loginWithGoogleUseCase()
        } catch (ex: Exception) {
            _viewState.update { it.copy(errorMessage = ex.message, isLoading = false) }
        }
        _viewState.update { it.copy(loginScreenIsLoading = false) }
    }

    fun clearErrorMessage() {
        _viewState.update { it.copy(errorMessage = null) }
    }

    fun onSendMagicLink(email: String) = viewModelScope.launch {
        if (email.matches(emailAddressRegex)) {
            _viewState.update { it.copy(showEmailError = false) }
            try {
                sendMagicLinkUseCase.invoke(email)
                _viewState.update { it.copy(showMagicLinkSentDialog = true) }
            } catch (ex: Exception) {
                _viewState.update { it.copy(errorMessage = ex.message) }
            }
        } else {
            _viewState.update { it.copy(showEmailError = true) }
        }
    }

    fun onDismissDialog() {
        _viewState.update { it.copy(showMagicLinkSentDialog = false) }
    }

    private var prevJob: Job? = null
    fun onEmailUpdate(newEmail: String) {
        _viewState.update { it.copy(email = newEmail) }
        prevJob?.cancel()
        prevJob = viewModelScope.launch {
            delay(1500)
            val isEmailValid = newEmail.matches(emailAddressRegex)
            _viewState.update {
                it.copy(
                    showEmailError = !isEmailValid,
                    isSubmitEnabled = isEmailValid
                )
            }
        }
    }

    fun onMagickLinkClick(url: String) = viewModelScope.launch {
        logging("AuthViewModel").d { "tryCompleteMagicLink: $url" }
        val result = loginWithMagicLinkUseCase(url)
        if (result is MagicLinkResult.Error) {
            _viewState.update { it.copy(errorMessage = result.message) }
        }
    }

    /**
     * Email address pattern, same as [android.util.Patterns.EMAIL_ADDRESS]
     */
    private val emailAddressRegex = Regex(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
}