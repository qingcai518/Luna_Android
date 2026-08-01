package jp.co.studio.kaka.ui.navigation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import jp.co.studio.kaka.data.local.datastore.TokenStore
import jp.co.studio.kaka.data.local.datastore.UserPreferencesDataStore
import jp.co.studio.kaka.domain.SessionManager
import jp.co.studio.kaka.domain.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Uses a real SessionManager backed by a real file-based DataStore (only TokenStore is faked,
 * same seam TokenAuthenticatorTest uses) rather than mocking SessionManager, since RootViewModel
 * is a near-transparent pass-through and the interesting behavior lives in SessionManager itself.
 */
class RootViewModelTest {

    private lateinit var tempDataStoreFile: File

    @Before
    fun setUp() {
        tempDataStoreFile = File.createTempFile("test_root_vm_user_prefs", ".preferences_pb")
    }

    @After
    fun tearDown() {
        tempDataStoreFile.delete()
    }

    private fun buildSessionManager(tokenStore: TokenStore): SessionManager {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { tempDataStoreFile })
        return SessionManager(tokenStore, UserPreferencesDataStore(dataStore))
    }

    private class FakeTokenStore(private var hasSession: Boolean) : TokenStore {
        private var accessToken: String? = if (hasSession) "access-token" else null
        private var refreshToken: String? = if (hasSession) "refresh-token" else null

        override fun getAccessToken(): String? = accessToken
        override fun getRefreshToken(): String? = refreshToken
        override fun saveTokens(accessToken: String, refreshToken: String) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken
            hasSession = true
        }
        override fun clear() {
            accessToken = null
            refreshToken = null
            hasSession = false
        }
        override fun hasSession(): Boolean = hasSession
    }

    @Test
    fun `isLoggedIn reflects an existing session at startup`() {
        val sessionManager = buildSessionManager(FakeTokenStore(hasSession = true))
        val viewModel = RootViewModel(sessionManager)

        assertEquals(true, viewModel.isLoggedIn.value)
    }

    @Test
    fun `isLoggedIn reflects no session at startup`() {
        val sessionManager = buildSessionManager(FakeTokenStore(hasSession = false))
        val viewModel = RootViewModel(sessionManager)

        assertEquals(false, viewModel.isLoggedIn.value)
    }

    @Test
    fun `isLoggedIn and forcedLogoutEvents are the exact SessionManager instances, not copies`() {
        val sessionManager = buildSessionManager(FakeTokenStore(hasSession = false))
        val viewModel = RootViewModel(sessionManager)

        assertSame(sessionManager.isLoggedIn, viewModel.isLoggedIn)
        assertSame(sessionManager.forcedLogoutEvents, viewModel.forcedLogoutEvents)
    }

    @Test
    fun `a successful login flips isLoggedIn to true`() = runBlocking {
        val sessionManager = buildSessionManager(FakeTokenStore(hasSession = false))
        val viewModel = RootViewModel(sessionManager)
        assertEquals(false, viewModel.isLoggedIn.value)

        sessionManager.onLoginSuccess("access", "refresh", User(1L, "qingcai518", "a@b.com", null))

        assertEquals(true, viewModel.isLoggedIn.value)
    }

    @Test
    fun `user-initiated logout flips isLoggedIn to false`() = runBlocking {
        val sessionManager = buildSessionManager(FakeTokenStore(hasSession = true))
        val viewModel = RootViewModel(sessionManager)
        assertEquals(true, viewModel.isLoggedIn.value)

        sessionManager.logout()

        assertEquals(false, viewModel.isLoggedIn.value)
    }

    @Test
    fun `a forced logout flips isLoggedIn synchronously and eventually emits a forced-logout event`() = runBlocking {
        val sessionManager = buildSessionManager(FakeTokenStore(hasSession = true))
        val viewModel = RootViewModel(sessionManager)

        sessionManager.forceLogout()

        // isLoggedIn is set synchronously inside forceLogout(); the event itself is emitted from
        // an internal background scope (forceLogout is called from a non-coroutine OkHttp thread),
        // so it must be awaited rather than asserted immediately.
        assertEquals(false, viewModel.isLoggedIn.value)
        withTimeout(2_000) {
            viewModel.forcedLogoutEvents.first()
        }
    }
}
