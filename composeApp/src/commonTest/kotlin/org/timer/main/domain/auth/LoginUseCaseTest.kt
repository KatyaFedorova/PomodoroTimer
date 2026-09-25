package org.timer.main.domain.auth

import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.auth.*
import kotlin.test.*

class LoginUseCaseTest {

    private lateinit var loginUseCase: DefaultLoginWithGoogleUseCase
    private lateinit var authGetway: FakeAuthGateway

    @BeforeTest
    fun setUp() {
        authGetway = FakeAuthGateway()
        loginUseCase = DefaultLoginWithGoogleUseCase(authGetway)
    }

    @Test
    fun `login should logged in user`() = runTest {
      loginUseCase.invoke()
        assertThat(authGetway.getAuthState().first()).isEqualTo(AuthState.Authenticated(id = "user_id", email = "email", name = null))
    }
    @Test
    fun `login should create new user`() = runTest {
        loginUseCase.invoke()
        assertThat(authGetway.createdUser).isEqualTo(Pair("user_id", "email"))
    }
}