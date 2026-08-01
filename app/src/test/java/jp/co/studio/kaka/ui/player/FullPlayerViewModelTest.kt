package jp.co.studio.kaka.ui.player

import jp.co.studio.kaka.domain.model.LyricLine
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.repository.LyricsRepository
import jp.co.studio.kaka.player.MediaControllerRepository
import jp.co.studio.kaka.player.PlayerRepeatMode
import jp.co.studio.kaka.player.PlayerUiState
import jp.co.studio.kaka.util.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FullPlayerViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun musicFixture(id: Long) = Music(
        id = id,
        title = "曲目$id",
        coverUrl = null,
        audioUrl = "https://resource.qingcai518.com/music/$id.mp3",
        releaseDate = null,
        durationSeconds = null,
        artist = null,
        category = null,
    )

    private class FakeLyricsRepository(private val result: ApiResult<List<LyricLine>>) : LyricsRepository {
        var callCount = 0
        var lastMusicId: Long? = null

        override suspend fun getLyrics(musicId: Long): ApiResult<List<LyricLine>> {
            callCount++
            lastMusicId = musicId
            return result
        }
    }

    private class FakeMediaControllerRepository : MediaControllerRepository {
        private val _uiState = MutableStateFlow(PlayerUiState())
        override val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

        val playPauseCallCount get() = calls.count { it == "playPause" }
        val calls = mutableListOf<String>()
        var lastSeekPositionMs: Long? = null
        var lastSeekQueueIndex: Int? = null
        var lastRepeatMode: PlayerRepeatMode? = null
        var lastShuffleEnabled: Boolean? = null

        fun setState(state: PlayerUiState) {
            _uiState.value = state
        }

        override fun playQueue(musics: List<Music>, startIndex: Int) {}
        override fun playPause() { calls += "playPause" }
        override fun seekTo(positionMs: Long) { calls += "seekTo"; lastSeekPositionMs = positionMs }
        override fun skipNext() { calls += "skipNext" }
        override fun skipPrevious() { calls += "skipPrevious" }
        override fun seekToQueueItem(index: Int) { calls += "seekToQueueItem"; lastSeekQueueIndex = index }
        override fun setRepeatMode(mode: PlayerRepeatMode) { calls += "setRepeatMode"; lastRepeatMode = mode }
        override fun setShuffleEnabled(enabled: Boolean) { calls += "setShuffleEnabled"; lastShuffleEnabled = enabled }
        override fun stopAndClear() {}
    }

    @Test
    fun `no current track keeps lyrics empty and never calls the repository`() = runTest(dispatcher) {
        val lyricsRepository = FakeLyricsRepository(ApiResult.Success(listOf(LyricLine(0L, "不应该被拉取"))))
        val viewModel = FullPlayerViewModel(FakeMediaControllerRepository(), lyricsRepository)
        advanceUntilIdle()

        assertTrue(viewModel.lyrics.value.isEmpty())
        assertEquals(0, lyricsRepository.callCount)
    }

    @Test
    fun `a track transition fetches and populates lyrics for the new track`() = runTest(dispatcher) {
        val lyricsRepository = FakeLyricsRepository(
            ApiResult.Success(listOf(LyricLine(0L, "第一行"), LyricLine(1000L, "第二行"))),
        )
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = FullPlayerViewModel(mediaControllerRepository, lyricsRepository)
        advanceUntilIdle()

        mediaControllerRepository.setState(PlayerUiState(queue = listOf(musicFixture(1L)), currentIndex = 0))
        advanceUntilIdle()

        assertEquals(1, lyricsRepository.callCount)
        assertEquals(1L, lyricsRepository.lastMusicId)
        assertEquals(2, viewModel.lyrics.value.size)
        assertEquals("第一行", viewModel.lyrics.value.first().text)
    }

    @Test
    fun `progress updates on the same track do not re-fetch lyrics`() = runTest(dispatcher) {
        val lyricsRepository = FakeLyricsRepository(ApiResult.Success(listOf(LyricLine(0L, "歌词"))))
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = FullPlayerViewModel(mediaControllerRepository, lyricsRepository)
        advanceUntilIdle()
        mediaControllerRepository.setState(PlayerUiState(queue = listOf(musicFixture(1L)), currentIndex = 0))
        advanceUntilIdle()
        assertEquals(1, lyricsRepository.callCount)

        // Same track (id unchanged), just the playback position ticking forward.
        mediaControllerRepository.setState(
            PlayerUiState(queue = listOf(musicFixture(1L)), currentIndex = 0, positionMs = 5_000L),
        )
        advanceUntilIdle()

        assertEquals(1, lyricsRepository.callCount)
    }

    @Test
    fun `switching to a different track clears then refetches lyrics for the new id`() = runTest(dispatcher) {
        val lyricsRepository = FakeLyricsRepository(ApiResult.Success(listOf(LyricLine(0L, "歌词"))))
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = FullPlayerViewModel(mediaControllerRepository, lyricsRepository)
        advanceUntilIdle()
        mediaControllerRepository.setState(PlayerUiState(queue = listOf(musicFixture(1L)), currentIndex = 0))
        advanceUntilIdle()

        mediaControllerRepository.setState(PlayerUiState(queue = listOf(musicFixture(2L)), currentIndex = 0))
        advanceUntilIdle()

        assertEquals(2, lyricsRepository.callCount)
        assertEquals(2L, lyricsRepository.lastMusicId)
    }

    @Test
    fun `queue becoming empty clears lyrics without an extra fetch`() = runTest(dispatcher) {
        val lyricsRepository = FakeLyricsRepository(ApiResult.Success(listOf(LyricLine(0L, "歌词"))))
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = FullPlayerViewModel(mediaControllerRepository, lyricsRepository)
        advanceUntilIdle()
        mediaControllerRepository.setState(PlayerUiState(queue = listOf(musicFixture(1L)), currentIndex = 0))
        advanceUntilIdle()
        assertEquals(1, viewModel.lyrics.value.size)

        mediaControllerRepository.setState(PlayerUiState())
        advanceUntilIdle()

        assertTrue(viewModel.lyrics.value.isEmpty())
        assertEquals(1, lyricsRepository.callCount)
    }

    @Test
    fun `a failed lyrics fetch leaves lyrics empty`() = runTest(dispatcher) {
        val lyricsRepository = FakeLyricsRepository(ApiResult.Error("100060", "歌词获取失败"))
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = FullPlayerViewModel(mediaControllerRepository, lyricsRepository)
        advanceUntilIdle()

        mediaControllerRepository.setState(PlayerUiState(queue = listOf(musicFixture(1L)), currentIndex = 0))
        advanceUntilIdle()

        assertEquals(1, lyricsRepository.callCount)
        assertTrue(viewModel.lyrics.value.isEmpty())
    }

    @Test
    fun `transport controls delegate directly to the media controller repository`() = runTest(dispatcher) {
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = FullPlayerViewModel(mediaControllerRepository, FakeLyricsRepository(ApiResult.Success(emptyList())))

        viewModel.playPause()
        viewModel.seekTo(1_500L)
        viewModel.skipNext()
        viewModel.skipPrevious()
        viewModel.seekToQueueItem(2)
        viewModel.setRepeatMode(PlayerRepeatMode.ALL)
        viewModel.setShuffleEnabled(true)

        assertEquals(
            listOf("playPause", "seekTo", "skipNext", "skipPrevious", "seekToQueueItem", "setRepeatMode", "setShuffleEnabled"),
            mediaControllerRepository.calls,
        )
        assertEquals(1_500L, mediaControllerRepository.lastSeekPositionMs)
        assertEquals(2, mediaControllerRepository.lastSeekQueueIndex)
        assertEquals(PlayerRepeatMode.ALL, mediaControllerRepository.lastRepeatMode)
        assertEquals(true, mediaControllerRepository.lastShuffleEnabled)
    }
}
