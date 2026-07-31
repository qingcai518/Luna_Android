package jp.co.studio.kaka.data.remote.interceptor

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import jp.co.studio.kaka.data.local.datastore.TokenStore
import jp.co.studio.kaka.data.local.datastore.UserPreferencesDataStore
import jp.co.studio.kaka.data.remote.api.AuthApiService
import jp.co.studio.kaka.data.remote.converter.SerializationConverterFactory
import jp.co.studio.kaka.domain.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Provider

/**
 * Exercises TokenAuthenticator against a real MockWebServer + real Retrofit stack (only the
 * token storage is faked, via the TokenStore seam extracted specifically for this test).
 */
class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var sessionManager: SessionManager
    private lateinit var authApiService: AuthApiService
    private lateinit var apiOkHttpClient: OkHttpClient
    private lateinit var tempDataStoreFile: File

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; coerceInputValues = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        tokenStore = FakeTokenStore(accessToken = "expired-access-token", refreshToken = "valid-refresh-token")

        tempDataStoreFile = File.createTempFile("test_user_prefs", ".preferences_pb")
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { tempDataStoreFile },
        )
        sessionManager = SessionManager(tokenStore, UserPreferencesDataStore(dataStore))

        val authRetrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(SerializationConverterFactory.create(json, "application/json".toMediaType()))
            .build()
        authApiService = authRetrofit.create(AuthApiService::class.java)

        val tokenAuthenticator = TokenAuthenticator(
            tokenStore = tokenStore,
            apiServiceProvider = Provider { authApiService },
            sessionManager = sessionManager,
            json = json,
        )

        apiOkHttpClient = OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
        tempDataStoreFile.delete()
    }

    @Test
    fun `401 triggers a refresh and the retried request succeeds with the new token`() {
        server.enqueue(MockResponse().setResponseCode(401)) // first attempt on the protected endpoint
        server.enqueue(
            MockResponse().setBody(
                """{"code":"000000","message":"","data":{"accessToken":"new-access-token","refreshToken":"new-refresh-token"}}""",
            ),
        ) // refresh-token call
        server.enqueue(MockResponse().setBody("""{"code":"000000","message":"","data":{"artists":[]}}""")) // retried request

        val request = okhttp3.Request.Builder().url(server.url("/artist")).build()
        val response = apiOkHttpClient.newCall(request).execute()

        assertEquals(200, response.code)
        assertEquals("new-access-token", tokenStore.getAccessToken())
        assertEquals("new-refresh-token", tokenStore.getRefreshToken())

        val refreshRequest = server.takeRequest() // consume the first 401 response's request
        assertEquals("/artist", refreshRequest.path)
        val actualRefreshCall = server.takeRequest()
        assertEquals("/users/refresh-token", actualRefreshCall.path)
    }

    @Test
    fun `concurrent 401s only trigger a single refresh call`() {
        // One 401 per concurrent request, then a single refresh response, then success responses.
        repeat(5) { server.enqueue(MockResponse().setResponseCode(401)) }
        server.enqueue(
            MockResponse().setBody(
                """{"code":"000000","message":"","data":{"accessToken":"new-access-token","refreshToken":"new-refresh-token"}}""",
            ),
        )
        repeat(5) { server.enqueue(MockResponse().setBody("""{"code":"000000","message":"","data":{"artists":[]}}""")) }

        val executor = Executors.newFixedThreadPool(5)
        val latch = CountDownLatch(5)
        val results = java.util.concurrent.ConcurrentLinkedQueue<Int>()

        repeat(5) {
            executor.submit {
                val request = okhttp3.Request.Builder().url(server.url("/artist")).build()
                val response = apiOkHttpClient.newCall(request).execute()
                results += response.code
                latch.countDown()
            }
        }
        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        assertEquals(5, results.size)
        assertEquals(listOf(200, 200, 200, 200, 200), results.sorted())

        // 5 initial (401) + 1 refresh + 5 retries = 11 requests total; exactly one must hit refresh-token.
        val refreshCallCount = (1..11).count { server.takeRequest(5, TimeUnit.SECONDS)?.path == "/users/refresh-token" }
        assertEquals(1, refreshCallCount)
    }

    @Test
    fun `refresh failure forces logout and the request is not retried`() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401)) // refresh-token call itself rejected

        val request = okhttp3.Request.Builder().url(server.url("/artist")).build()
        val response = apiOkHttpClient.newCall(request).execute()

        assertEquals(401, response.code)
        assertEquals(false, sessionManager.isLoggedIn.value)
    }

    private class FakeTokenStore(accessToken: String?, refreshToken: String?) : TokenStore {
        private var accessToken = accessToken
        private var refreshToken = refreshToken

        override fun getAccessToken(): String? = accessToken
        override fun getRefreshToken(): String? = refreshToken
        override fun saveTokens(accessToken: String, refreshToken: String) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken
        }
        override fun clear() {
            accessToken = null
            refreshToken = null
        }
        override fun hasSession(): Boolean = accessToken != null
    }
}
