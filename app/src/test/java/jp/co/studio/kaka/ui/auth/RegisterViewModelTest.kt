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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

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
        private val registerResult: ApiResult<Unit> = ApiResult.Success(Unit),
    ) : AuthRepository {
        var registerCallCount = 0
        var lastRegisterArgs: Triple<String, String, String>? = null
        override val currentUser = MutableStateFlow<User?>(null)
        override val isLoggedIn = MutableStateFlow(false)

        override suspend fun login(username: String, password: String): ApiResult<User> =
            ApiResult.Success(User(1L, username, "", null))

        override suspend fun register(username: String, password: String, email: String): ApiResult<Unit> {
            registerCallCount++
            lastRegisterArgs = Triple(username, password, email)
            return registerResult
        }

        override suspend fun logout() {}
    }

    private fun fillValidForm(viewModel: RegisterViewModel) {
        viewModel.onUsernameChange("qingcai518")
        viewModel.onEmailChange("qingcai518@gmail.com")
        viewModel.onPasswordChange("wy03237462")
        viewModel.onConfirmPasswordChange("wy03237462")
    }

    @Test
    fun `register success marks registerSucceeded`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(ApiResult.Success(Unit))
        val viewModel = RegisterViewModel(repository)
        fillValidForm(viewModel)

        viewModel.register()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.registerSucceeded)
        assertFalse(state.isLoading)
        assertEquals(1, repository.registerCallCount)
        assertEquals(Triple("qingcai518", "wy03237462", "qingcai518@gmail.com"), repository.lastRegisterArgs)
    }

    @Test
    fun `register error surfaces the backend message`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(ApiResult.Error("100030", "用户名已存在"))
        val viewModel = RegisterViewModel(repository)
        fillValidForm(viewModel)

        viewModel.register()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.registerSucceeded)
        assertEquals("用户名已存在", state.errorMessage)
    }

    @Test
    fun `register is a no-op when the email is missing an at-sign`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = RegisterViewModel(repository)
        viewModel.onUsernameChange("qingcai518")
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("wy03237462")
        viewModel.onConfirmPasswordChange("wy03237462")

        viewModel.register()
        advanceUntilIdle()

        assertEquals(0, repository.registerCallCount)
    }

    @Test
    fun `register is a no-op when password and confirmPassword differ`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = RegisterViewModel(repository)
        viewModel.onUsernameChange("qingcai518")
        viewModel.onEmailChange("qingcai518@gmail.com")
        viewModel.onPasswordChange("wy03237462")
        viewModel.onConfirmPasswordChange("different-password")

        viewModel.register()
        advanceUntilIdle()

        assertEquals(0, repository.registerCallCount)
    }

    @Test
    fun `editing any field after an error clears the error message`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(ApiResult.Error("100030", "用户名已存在"))
        val viewModel = RegisterViewModel(repository)
        fillValidForm(viewModel)
        viewModel.register()
        advanceUntilIdle()
        assertEquals("用户名已存在", viewModel.uiState.value.errorMessage)

        viewModel.onUsernameChange("qingcai519")

        assertEquals(null, viewModel.uiState.value.errorMessage)
    }
}
