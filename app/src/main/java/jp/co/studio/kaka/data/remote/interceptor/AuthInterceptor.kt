package jp.co.studio.kaka.data.remote.interceptor

import jp.co.studio.kaka.data.local.datastore.TokenStore
import jp.co.studio.kaka.util.Constants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches Authorization to every request except login/register/refresh-token. Confirmed
 * against LunaAPI's SecurityConfig: anyRequest().authenticated() covers everything else,
 * including endpoints that look "public" at a glance like /artist and /category.
 */
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimStart('/')
        if (Constants.NO_AUTH_PATHS.any { path.endsWith(it) }) {
            return chain.proceed(request)
        }
        val token = tokenStore.getAccessToken() ?: return chain.proceed(request)
        val authedRequest = request.newBuilder()
            .header(Constants.AUTH_HEADER, Constants.BEARER_PREFIX + token)
            .build()
        return chain.proceed(authedRequest)
    }
}
