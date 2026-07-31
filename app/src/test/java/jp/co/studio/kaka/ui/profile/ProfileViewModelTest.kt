package jp.co.studio.kaka.ui.profile

import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.User
import jp.co.studio.kaka.domain.repository.AuthRepository
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.player.MediaControllerRepository
import jp.co.studio.kaka.player.PlayerRepeatMode
import jp.co.studio.kaka.player.PlayerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun downloadedMusicFixture(id: Long) = DownloadedMusic(
        id = id,
        title = "曲目$id",
        localCoverPath = null,
        localAudioPath = "/data/user/0/jp.co.studio.kaka/files/music/$id.mp3",
        localLyricsPath = null,
        releaseDate = null,
        durationSeconds = null,
        artistName = "Unknown Artist",
        categoryName = "Unknown",
        downloadDate = 0L,
    )

    private class FakeAuthRepository(
        user: User?,
        loggedIn: Boolean,
        private val callLog: MutableList<String>,
    ) : AuthRepository {
        override val currentUser = MutableStateFlow(user)
        override val isLoggedIn = MutableStateFlow(loggedIn)

        override suspend fun login(username: String, password: String) =
            jp.co.studio.kaka.util.ApiResult.Success(User(userId = 0L, username = username, email = "", avatarUrl = null))

        override suspend fun register(username: String, password: String, email: String) =
            jp.co.studio.kaka.util.ApiResult.Success(Unit)

        override suspend fun logout() {
            callLog += "authRepository.logout"
        }
    }

    private class FakeDownloadRepository(
        initialDownloaded: List<DownloadedMusic> = emptyList(),
    ) : DownloadRepository {
        override val downloadStates = MutableStateFlow<Map<Long, DownloadState>>(emptyMap())
        override val downloadedMusics = MutableStateFlow(initialDownloaded)

        override fun download(music: Music) {}
        override suspend fun delete(musicId: Long) {}
        override suspend fun reconcile() {}
    }

    private class FakeMediaControllerRepository(
        private val callLog: MutableList<String>,
    ) : MediaControllerRepository {
        private val _uiState = MutableStateFlow(PlayerUiState())
        override val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

        override fun playQueue(musics: List<Music>, startIndex: Int) {}
        override fun playPause() {}
        override fun seekTo(positionMs: Long) {}
        override fun skipNext() {}
        override fun skipPrevious() {}
        override fun seekToQueueItem(index: Int) {}
        override fun setRepeatMode(mode: PlayerRepeatMode) {}
        override fun setShuffleEnabled(enabled: Boolean) {}

        override fun stopAndClear() {
            callLog += "mediaControllerRepository.stopAndClear"
        }
    }

    @Test
    fun `combines current user, login state, and downloaded count into one state`() = runTest(dispatcher) {
        val user = User(userId = 1L, username = "qingcai518", email = "qingcai518@gmail.com", avatarUrl = null)
        val authRepository = FakeAuthRepository(user, loggedIn = true, callLog = mutableListOf())
        val downloadRepository = FakeDownloadRepository(listOf(downloadedMusicFixture(1L), downloadedMusicFixture(2L)))
        val mediaControllerRepository = FakeMediaControllerRepository(mutableListOf())
        val viewModel = ProfileViewModel(authRepository, downloadRepository, mediaControllerRepository)

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("qingcai518", state.username)
        assertEquals("qingcai518@gmail.com", state.email)
        assertTrue(state.isLoggedIn)
        assertEquals(2, state.downloadedCount)
        job.cancel()
    }

    @Test
    fun `logged-out state reflects a null user`() = runTest(dispatcher) {
        val authRepository = FakeAuthRepository(user = null, loggedIn = false, callLog = mutableListOf())
        val downloadRepository = FakeDownloadRepository(emptyList())
        val mediaControllerRepository = FakeMediaControllerRepository(mutableListOf())
        val viewModel = ProfileViewModel(authRepository, downloadRepository, mediaControllerRepository)

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoggedIn)
        assertEquals(null, state.username)
        assertEquals(0, state.downloadedCount)
        job.cancel()
    }

    @Test
    fun `logout stops playback before clearing the session - fixes the iOS gap of music surviving logout`() = runTest(dispatcher) {
        val callLog = mutableListOf<String>()
        val authRepository = FakeAuthRepository(user = null, loggedIn = true, callLog = callLog)
        val downloadRepository = FakeDownloadRepository()
        val mediaControllerRepository = FakeMediaControllerRepository(callLog)
        val viewModel = ProfileViewModel(authRepository, downloadRepository, mediaControllerRepository)

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(listOf("mediaControllerRepository.stopAndClear", "authRepository.logout"), callLog)
    }
}
