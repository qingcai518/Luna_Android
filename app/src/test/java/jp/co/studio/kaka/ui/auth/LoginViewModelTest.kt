package jp.co.studio.kaka.ui.auth

import jp.co.studio.kaka.domain.model.User
import jp.co.studio.kaka.domain.repository.AuthRepository
import jp.co.studio.kaka.util.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeAuthRepository(
        private val loginResult: ApiResult<User> = ApiResult.Success(User(1L, "qingcai518", "a@b.com", null)),
    ) : AuthRepository {
        var loginCallCount = 0
        override val currentUser = MutableStateFlow<User?>(null)
        override val isLoggedIn = MutableStateFlow(false)

        override suspend fun login(username: String, password: String): ApiResult<User> {
            loginCallCount++
            return loginResult
        }

        override suspend fun register(username: String, password: String, email: String): ApiResult<Unit> =
            ApiResult.Success(Unit)

        override suspend fun logout() {}
    }

    @Test
    fun `login success marks loginSucceeded and clears loading`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(ApiResult.Success(User(1L, "qingcai518", "a@b.com", null)))
        val viewModel = LoginViewModel(repository)
        viewModel.onUsernameChange("qingcai518")
        viewModel.onPasswordChange("wy03237462")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.loginSucceeded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `login error surfaces the backend message`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(ApiResult.Error("100002", "用户名或密码错误"))
        val viewModel = LoginViewModel(repository)
        viewModel.onUsernameChange("qingcai518")
        viewModel.onPasswordChange("wrong-password")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loginSucceeded)
        assertEquals("用户名或密码错误", state.errorMessage)
    }

    @Test
    fun `network error surfaces a generic connectivity message`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(ApiResult.NetworkError(IOException("timeout")))
        val viewModel = LoginViewModel(repository)
        viewModel.onUsernameChange("qingcai518")
        viewModel.onPasswordChange("wy03237462")

        viewModel.login()
        advanceUntilIdle()

        assertEquals("网络连接失败，请检查网络后重试", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `login is a no-op while username or password is blank`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository)
        viewModel.onUsernameChange("qingcai518")
        // password left blank - isSubmitEnabled is false

        viewModel.login()
        advanceUntilIdle()

        assertEquals(0, repository.loginCallCount)
    }

    @Test
    fun `editing a field after an error clears the error message`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(ApiResult.Error("100002", "用户名或密码错误"))
        val viewModel = LoginViewModel(repository)
        viewModel.onUsernameChange("qingcai518")
        viewModel.onPasswordChange("wrong-password")
        viewModel.login()
        advanceUntilIdle()
        assertEquals("用户名或密码错误", viewModel.uiState.value.errorMessage)

        viewModel.onPasswordChange("wrong-password-2")

        assertNull(viewModel.uiState.value.errorMessage)
    }
}
