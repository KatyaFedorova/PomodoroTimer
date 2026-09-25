package org.timer.main.domain.auth
import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import org.timer.main.domain.auth.*

class IsLoggedInUseCaseTest {

    private lateinit var isLoggedInUseCase: DefaultGetAuthStateUseCase
    private lateinit var authGateway: FakeAuthGateway

    @BeforeTest
    fun setUp() {
        authGateway = FakeAuthGateway()
        isLoggedInUseCase = DefaultGetAuthStateUseCase(authGateway)
    }

    @Test
    fun `is logged in state should be loading when the user hasn't logged yet`() = runTest {
        val actualResult = isLoggedInUseCase.invoke().first()
        assertThat(actualResult).isEqualTo(AuthState.Loading)
    }

    @Test
    fun `is logged in state should be autentificated when the user has successfully logged in`() = runTest {
        authGateway.loginWithGoogle()
        val actualResult = isLoggedInUseCase.invoke().first()
        assertThat(actualResult).isEqualTo(AuthState.Authenticated(id = "user_id", email = "email", name = null))
    }

    @Test
    fun `is logged in state should be Not Authenticated when the user has successfully logout`() = runTest {
        authGateway.loginWithGoogle()
        authGateway.logout()
        val actualResult = isLoggedInUseCase.invoke().first()
        assertThat(actualResult).isEqualTo(AuthState.NotAuthenticated)
    }
}