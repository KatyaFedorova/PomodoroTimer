package org.timer.main.auth

import com.varabyte.truthish.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.timer.main.auth.*
import org.timer.main.domain.auth.*
import org.timer.main.domain.timer.*
import org.timer.main.domain.auth.*
import kotlin.test.*

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AuthViewModel
    private lateinit var authGateway: FakeAuthGateway

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authGateway = FakeAuthGateway()
        viewModel = AuthViewModel(
            DefaultLoginWithGoogleUseCase(authGateway),
            DefaultSendMagicLinkUseCase(authGateway),
            DefaultMagicLinkLoginUseCase(authGateway),
            DefaultGetAuthStateUseCase(authGateway)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be user is not logged in and loading shown`() = runTest {
        val initialState = viewModel.viewState.first()
        assertThat(initialState.isLoggedIn).isFalse()
        assertThat(initialState.isLoading).isTrue()
    }

    @Test
    fun `user click login should update state to user is logged in`() = runTest {
        viewModel.onLoginClick()
        advanceUntilIdle()
        val viewState = viewModel.viewState.value
        assertThat(viewState.isLoggedIn).isTrue()
        assertThat(viewState.isLoading).isFalse()
    }

    @Test
    fun `user auth state is  Authenticated should update screen state to logged in and not loading`() = runTest {
            authGateway.flow.emit(AuthState.Authenticated("user_id", "email", null))
            advanceUntilIdle()
            val viewState = viewModel.viewState.value
            assertThat(viewState.isLoggedIn).isTrue()
            assertThat(viewState.isLoading).isFalse()
        }

    @Test
    fun `user auth state is Not Authenticated should update screen state to not logged in and not loading`() = runTest {
        authGateway.flow.emit(AuthState.NotAuthenticated)
        advanceUntilIdle()
        val viewState = viewModel.viewState.first()
        assertThat(viewState.isLoggedIn).isFalse()
        assertThat(viewState.isLoading).isFalse()
    }

    @Test
    fun `user auth state is Loading should update screen state to loading`() = runTest {
        authGateway.flow.emit(AuthState.Loading)
        advanceUntilIdle()
        val viewState = viewModel.viewState.first()
        assertThat(viewState.isLoading).isTrue()
    }


}