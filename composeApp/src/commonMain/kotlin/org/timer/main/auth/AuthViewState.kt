package org.timer.main.auth

data class AuthViewState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val loginScreenIsLoading: Boolean = false, //todo
    val errorMessage: String?=null,

    val email: String = "",
    val isSubmitEnabled: Boolean = false,
    val showEmailError: Boolean = false,
    val showMagicLinkSentDialog: Boolean = false,
)
