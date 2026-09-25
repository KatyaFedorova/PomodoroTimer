package org.timer.main.domain.auth

import com.varabyte.truthish.*
import kotlinx.coroutines.test.*
import org.timer.main.domain.auth.*
import org.timer.main.domain.timer.*
import kotlin.test.*

class AuthMagicLinkUseCasesTest {

    private lateinit var authGateway: FakeAuthGateway

    @BeforeTest
    fun setUp() {
        authGateway = FakeAuthGateway()
    }

    @Test
    fun `sending a magic link forwards the email to the gateway`() = runTest {
        DefaultSendMagicLinkUseCase(authGateway).invoke("cat@paws.com")

        assertThat(authGateway.sentMagicLinkEmail).isEqualTo("cat@paws.com")
    }

    @Test
    fun `completing the magic link sign-in succeeds and creates the user`() = runTest {
        val result = DefaultMagicLinkLoginUseCase(authGateway).invoke("https://link")

        assertThat(result).isEqualTo(MagicLinkResult.Success)
        assertThat(authGateway.createdUser).isEqualTo("user_id" to "email")
    }
}
