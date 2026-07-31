package jp.co.studio.kaka.domain

import jp.co.studio.kaka.data.local.datastore.TokenStore
import jp.co.studio.kaka.data.local.datastore.UserPreferencesDataStore
import jp.co.studio.kaka.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the login session. [TokenAuthenticator] calls [forceLogout] from a
 * plain OkHttp thread (not a coroutine) when refresh fails outright, so that path must be
 * safe to call synchronously; the DataStore/user-cache cleanup it triggers happens on an
 * internal scope instead of blocking the caller.
 */
@Singleton
class SessionManager @Inject constructor(
    private val tokenStore: TokenStore,
    private val userPreferencesDataStore: UserPreferencesDataStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isLoggedIn = MutableStateFlow(tokenStore.hasSession())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * Emitted only when the session is torn down by the network layer (refresh token rejected),
     * as opposed to a user tapping "logout" - collectors (LunaRoot) should navigate to the
     * login screen. Stopping playback is the caller's responsibility (player module must not be
     * a dependency of this class to avoid a domain -> player cycle).
     */
    private val _forcedLogoutEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forcedLogoutEvents: SharedFlow<Unit> = _forcedLogoutEvents.asSharedFlow()

    suspend fun onLoginSuccess(accessToken: String, refreshToken: String, user: User) {
        tokenStore.saveTokens(accessToken, refreshToken)
        userPreferencesDataStore.saveUser(user)
        _isLoggedIn.value = true
    }

    /** User-initiated logout (Profile screen). Caller must stop playback first. */
    suspend fun logout() {
        tokenStore.clear()
        userPreferencesDataStore.clearUser()
        _isLoggedIn.value = false
    }

    /** Called synchronously from [okhttp3.Authenticator.authenticate] when refresh fails. */
    fun forceLogout() {
        tokenStore.clear()
        _isLoggedIn.value = false
        scope.launch {
            userPreferencesDataStore.clearUser()
            _forcedLogoutEvents.emit(Unit)
        }
    }
}
