package jp.co.studio.kaka.data.remote.interceptor

import jp.co.studio.kaka.data.local.datastore.TokenStore
import jp.co.studio.kaka.data.remote.api.AuthApiService
import jp.co.studio.kaka.data.remote.dto.RefreshTokenRequest
import jp.co.studio.kaka.domain.SessionManager
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.Constants
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * Handles 401s with: single-flight refresh under concurrency, retry-once, and forced logout
 * when refresh itself fails (including refresh's own 401, which may have a JSON body OR none -
 * safeApiCall already tolerates both). `apiServiceProvider` is a Provider, not a direct
 * AuthApiService, to break the dependency cycle: this Authenticator is installed on the
 * OkHttpClient that AuthApiService's Retrofit instance is built from.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val apiServiceProvider: Provider<AuthApiService>,
    private val sessionManager: SessionManager,
    private val json: Json,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.trimStart('/').endsWith("users/refresh-token")) {
            return null
        }
        if (responseCount(response) >= 2) {
            return null
        }

        val failedAccessToken = tokenStore.getAccessToken()

        return runBlocking {
            mutex.withLock {
                val currentToken = tokenStore.getAccessToken()
                if (currentToken != null && currentToken != failedAccessToken) {
                    // A concurrent request already refreshed while we waited for the lock.
                    return@withLock buildRequest(response.request, currentToken)
                }

                val refreshToken = tokenStore.getRefreshToken()
                if (refreshToken == null) {
                    sessionManager.forceLogout()
                    return@withLock null
                }

                when (val result = safeApiCall(json) {
                    apiServiceProvider.get().refreshToken(RefreshTokenRequest(refreshToken))
                }) {
                    is ApiResult.Success -> {
                        tokenStore.saveTokens(result.data.accessToken, result.data.refreshToken)
                        buildRequest(response.request, result.data.accessToken)
                    }
                    else -> {
                        sessionManager.forceLogout()
                        null
                    }
                }
            }
        }
    }

    private fun buildRequest(request: Request, accessToken: String): Request =
        request.newBuilder()
            .header(Constants.AUTH_HEADER, Constants.BEARER_PREFIX + accessToken)
            .build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
