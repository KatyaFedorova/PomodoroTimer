package org.timer.main.domain.auth
import com.varabyte.truthish.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import org.timer.main.domain.auth.*

class LogoutUseCaseTest {

    private lateinit var logoutUseCase: DefaultLogoutUseCase
    private lateinit var authGateway: FakeAuthGateway

    @BeforeTest
    fun setUp() {
        authGateway = FakeAuthGateway()
        logoutUseCase = DefaultLogoutUseCase(authGateway)
    }

    @Test
    fun `logout should logout user`() = runTest {
        logoutUseCase.invoke()
        assertThat(authGateway.getAuthState().first()).isEqualTo(AuthState.NotAuthenticated)
    }
}