package jp.co.studio.kaka.domain.repository

import jp.co.studio.kaka.domain.model.User
import jp.co.studio.kaka.util.ApiResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    val isLoggedIn: Flow<Boolean>

    suspend fun login(username: String, password: String): ApiResult<User>
    suspend fun register(username: String, password: String, email: String): ApiResult<Unit>

    /** User-initiated logout. Callers must stop playback before invoking this. */
    suspend fun logout()
}
