package jp.co.studio.kaka.ui.downloaded

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.player.MediaControllerRepository
import jp.co.studio.kaka.player.PlayerRepeatMode
import jp.co.studio.kaka.player.PlayerUiState
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadedViewModelTest {

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
        localCoverPath = "/data/user/0/jp.co.studio.kaka/files/covers/$id.jpg",
        localAudioPath = "/data/user/0/jp.co.studio.kaka/files/music/$id.mp3",
        localLyricsPath = null,
        releaseDate = null,
        durationSeconds = null,
        artistName = "王菲",
        categoryName = "流行",
        downloadDate = 0L,
    )

    private class FakeDownloadRepository(
        initialDownloaded: List<DownloadedMusic> = emptyList(),
    ) : DownloadRepository {
        override val downloadStates = MutableStateFlow<Map<Long, DownloadState>>(emptyMap())
        override val downloadedMusics = MutableStateFlow(initialDownloaded)
        var reconcileCallCount = 0
        val deletedIds = mutableListOf<Long>()

        override fun download(music: Music) {}

        override suspend fun delete(musicId: Long) {
            deletedIds += musicId
        }

        override suspend fun reconcile() {
            reconcileCallCount++
        }
    }

    private class FakeMediaControllerRepository(initialState: PlayerUiState = PlayerUiState()) : MediaControllerRepository {
        private val _uiState = MutableStateFlow(initialState)
        override val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
        val playQueueCalls = mutableListOf<Pair<List<Music>, Int>>()
        var skipNextCallCount = 0

        override fun playQueue(musics: List<Music>, startIndex: Int) {
            playQueueCalls += musics to startIndex
        }

        override fun playPause() {}
        override fun seekTo(positionMs: Long) {}

        override fun skipNext() {
            skipNextCallCount++
        }

        override fun skipPrevious() {}
        override fun seekToQueueItem(index: Int) {}
        override fun setRepeatMode(mode: PlayerRepeatMode) {}
        override fun setShuffleEnabled(enabled: Boolean) {}
        override fun stopAndClear() {}
    }

    @Test
    fun `init reconciles storage and populates the list from the downloaded-music flow`() = runTest(dispatcher) {
        val downloadRepository = FakeDownloadRepository(listOf(downloadedMusicFixture(1L), downloadedMusicFixture(2L)))
        val viewModel = DownloadedViewModel(downloadRepository, FakeMediaControllerRepository())
        advanceUntilIdle()

        assertEquals(1, downloadRepository.reconcileCallCount)
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.musics.size)
    }

    @Test
    fun `play maps downloaded records to playable Music via file URIs and placeholder artist-category`() = runTest(dispatcher) {
        val mediaControllerRepository = FakeMediaControllerRepository()
        val viewModel = DownloadedViewModel(FakeDownloadRepository(), mediaControllerRepository)
        val musics = listOf(downloadedMusicFixture(1L), downloadedMusicFixture(2L))

        viewModel.play(musics, index = 1)

        assertEquals(1, mediaControllerRepository.playQueueCalls.size)
        val (queue, startIndex) = mediaControllerRepository.playQueueCalls.first()
        assertEquals(1, startIndex)
        assertEquals("file:///data/user/0/jp.co.studio.kaka/files/music/1.mp3", queue[0].audioUrl)
        assertEquals("王菲", queue[0].artist?.name)
        assertEquals("流行", queue[0].category?.name)
    }

    @Test
    fun `deleting a track that is not currently playing does not skip`() = runTest(dispatcher) {
        val downloadRepository = FakeDownloadRepository(listOf(downloadedMusicFixture(1L)))
        val mediaControllerRepository = FakeMediaControllerRepository(
            initialState = PlayerUiState(queue = listOf(downloadedMusicFixture(2L).let {
                Music(it.id, it.title, it.localCoverPath, "file://${it.localAudioPath}", it.releaseDate, it.durationSeconds, null, null)
            }), currentIndex = 0),
        )
        val viewModel = DownloadedViewModel(downloadRepository, mediaControllerRepository)
        advanceUntilIdle()

        viewModel.delete(musicId = 1L)
        advanceUntilIdle()

        assertEquals(0, mediaControllerRepository.skipNextCallCount)
        assertEquals(listOf(1L), downloadRepository.deletedIds)
    }

    @Test
    fun `deleting the currently playing track skips to the next one first`() = runTest(dispatcher) {
        val downloadRepository = FakeDownloadRepository(listOf(downloadedMusicFixture(1L)))
        val playingMusic = downloadedMusicFixture(1L).let {
            Music(it.id, it.title, it.localCoverPath, "file://${it.localAudioPath}", it.releaseDate, it.durationSeconds, null, null)
        }
        val mediaControllerRepository = FakeMediaControllerRepository(
            initialState = PlayerUiState(queue = listOf(playingMusic), currentIndex = 0),
        )
        val viewModel = DownloadedViewModel(downloadRepository, mediaControllerRepository)
        advanceUntilIdle()

        viewModel.delete(musicId = 1L)
        advanceUntilIdle()

        assertEquals(1, mediaControllerRepository.skipNextCallCount)
        assertEquals(listOf(1L), downloadRepository.deletedIds)
    }
}
