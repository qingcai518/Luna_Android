package jp.co.studio.kaka.data.repository

import jp.co.studio.kaka.data.local.datastore.UserPreferencesDataStore
import jp.co.studio.kaka.data.remote.api.AuthApiService
import jp.co.studio.kaka.data.remote.dto.LoginRequest
import jp.co.studio.kaka.data.remote.dto.RegisterRequest
import jp.co.studio.kaka.domain.SessionManager
import jp.co.studio.kaka.domain.model.User
import jp.co.studio.kaka.domain.repository.AuthRepository
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val json: Json,
) : AuthRepository {

    override val currentUser: Flow<User?> = userPreferencesDataStore.currentUser
    override val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn

    override suspend fun login(username: String, password: String): ApiResult<User> {
        return when (val result = safeApiCall(json) { apiService.login(LoginRequest(username, password)) }) {
            is ApiResult.Success -> {
                val dto = result.data
                val user = User(userId = dto.userid, username = dto.username, email = dto.email, avatarUrl = dto.avatarUrl)
                sessionManager.onLoginSuccess(dto.accessToken, dto.refreshToken, user)
                ApiResult.Success(user)
            }
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun register(username: String, password: String, email: String): ApiResult<Unit> {
        return when (val result = safeApiCall(json) { apiService.register(RegisterRequest(username, password, email)) }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun logout() {
        sessionManager.logout()
    }
}
