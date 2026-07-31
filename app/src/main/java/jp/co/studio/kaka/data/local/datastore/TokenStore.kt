package jp.co.studio.kaka.data.local.datastore

/**
 * Synchronous token storage contract - deliberately NOT DataStore (which is Flow/coroutine
 * based). okhttp3.Authenticator.authenticate() is called by OkHttp on a plain background
 * thread, not a coroutine, so token reads/writes must be fast and synchronous. Extracted as an
 * interface (implemented by [SecureTokenStore]) so [jp.co.studio.kaka.data.remote.interceptor.TokenAuthenticator]
 * can be unit-tested with an in-memory fake instead of a real Android Keystore-backed store.
 */
interface TokenStore {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?

    /** Access + refresh tokens must be written together: refresh tokens rotate server-side, so the old one is dead the instant a new pair is issued. */
    fun saveTokens(accessToken: String, refreshToken: String)
    fun clear()
    fun hasSession(): Boolean
}
