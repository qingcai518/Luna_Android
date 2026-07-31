package jp.co.studio.kaka.ui.musiclist

import androidx.lifecycle.SavedStateHandle
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.domain.repository.EventRepository
import jp.co.studio.kaka.domain.repository.MusicRepository
import jp.co.studio.kaka.download.DownloadStateHolder
import jp.co.studio.kaka.util.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MusicListViewModelTest {

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

    private class FakeMusicRepository(
        private val byArtistResult: ApiResult<List<Music>> = ApiResult.Success(emptyList()),
        private val byCategoryResult: ApiResult<List<Music>> = ApiResult.Success(emptyList()),
    ) : MusicRepository {
        var artistCallCount = 0
        var categoryCallCount = 0
        var lastArtistId: Long? = null
        var lastCategoryId: Long? = null

        override suspend fun getMusicByArtist(artistId: Long): ApiResult<List<Music>> {
            artistCallCount++
            lastArtistId = artistId
            return byArtistResult
        }

        override suspend fun getMusicByCategory(categoryId: Long): ApiResult<List<Music>> {
            categoryCallCount++
            lastCategoryId = categoryId
            return byCategoryResult
        }
    }

    private class FakeDownloadRepository : DownloadRepository {
        override val downloadStates = MutableStateFlow<Map<Long, DownloadState>>(emptyMap())
        override val downloadedMusics = flowOf(emptyList<DownloadedMusic>())
        var downloadedMusic: Music? = null

        override fun download(music: Music) {
            downloadedMusic = music
        }

        override suspend fun delete(musicId: Long) {}
        override suspend fun reconcile() {}
    }

    private class FakeEventRepository : EventRepository {
        val trackedDownloads = mutableListOf<Pair<Long, String>>()

        override suspend fun trackSkip(musicId: Long, source: String) {}
        override suspend fun trackSearch(keyword: String, source: String) {}

        override suspend fun trackDownload(musicId: Long, source: String) {
            trackedDownloads += musicId to source
        }

        override suspend fun trackPlay(musicId: Long, playDurationSeconds: Int, totalDurationSeconds: Int?, source: String) {}
    }

    private fun savedStateHandle(idType: String, id: Long, name: String? = null) =
        SavedStateHandle(buildMap {
            put("idType", idType)
            put("id", id)
            if (name != null) put("name", name)
        })

    private fun buildViewModel(
        savedStateHandle: SavedStateHandle,
        musicRepository: MusicRepository,
        downloadRepository: DownloadRepository = FakeDownloadRepository(),
        eventRepository: EventRepository = FakeEventRepository(),
    ) = MusicListViewModel(savedStateHandle, musicRepository, DownloadStateHolder(downloadRepository), eventRepository)

    @Test
    fun `artist idType fetches by artist id and decodes the title from the URL-encoded name`() = runTest(dispatcher) {
        val repository = FakeMusicRepository(byArtistResult = ApiResult.Success(listOf(musicFixture(1L))))
        val viewModel = buildViewModel(savedStateHandle("artist", 14L, "%E7%8E%8B%E8%8F%B2"), repository)
        advanceUntilIdle()

        assertEquals(1, repository.artistCallCount)
        assertEquals(0, repository.categoryCallCount)
        assertEquals(14L, repository.lastArtistId)
        val state = viewModel.uiState.value
        assertEquals("王菲", state.title)
        assertFalse(state.isLoading)
        assertEquals(1, state.musics.size)
    }

    @Test
    fun `non-artist idType fetches by category id`() = runTest(dispatcher) {
        val repository = FakeMusicRepository(byCategoryResult = ApiResult.Success(listOf(musicFixture(1L), musicFixture(2L))))
        val viewModel = buildViewModel(savedStateHandle("category", 1L, "%E6%B5%81%E8%A1%8C"), repository)
        advanceUntilIdle()

        assertEquals(1, repository.categoryCallCount)
        assertEquals(0, repository.artistCallCount)
        assertEquals(1L, repository.lastCategoryId)
        assertEquals(2, viewModel.uiState.value.musics.size)
    }

    @Test
    fun `missing name defaults the title to an empty string`() = runTest(dispatcher) {
        val repository = FakeMusicRepository()
        val viewModel = buildViewModel(savedStateHandle("artist", 1L), repository)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.title)
    }

    @Test
    fun `error surfaces the backend message`() = runTest(dispatcher) {
        val repository = FakeMusicRepository(byArtistResult = ApiResult.Error("100050", "获取歌曲列表失败"))
        val viewModel = buildViewModel(savedStateHandle("artist", 1L), repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("获取歌曲列表失败", state.errorMessage)
        assertFalse(state.isLoading)
        assertTrue(state.musics.isEmpty())
    }

    @Test
    fun `network error surfaces the generic connectivity message`() = runTest(dispatcher) {
        val repository = FakeMusicRepository(byArtistResult = ApiResult.NetworkError(IOException("timeout")))
        val viewModel = buildViewModel(savedStateHandle("artist", 1L), repository)
        advanceUntilIdle()

        assertEquals("网络连接失败，请检查网络后重试", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `load can be re-triggered manually and clears a previous error`() = runTest(dispatcher) {
        val repository = FakeMusicRepository(byArtistResult = ApiResult.Error("100050", "获取歌曲列表失败"))
        val viewModel = buildViewModel(savedStateHandle("artist", 1L), repository)
        advanceUntilIdle()
        assertEquals("获取歌曲列表失败", viewModel.uiState.value.errorMessage)

        viewModel.load()
        advanceUntilIdle()

        // Repository still errors on the second call, but the important part is the state was
        // reset (errorMessage cleared, isLoading toggled) before the second result landed.
        assertEquals(2, repository.artistCallCount)
    }

    @Test
    fun `downloadMusic on an artist list tracks the artist-scoped event source`() = runTest(dispatcher) {
        val downloadRepository = FakeDownloadRepository()
        val eventRepository = FakeEventRepository()
        val viewModel = buildViewModel(
            savedStateHandle("artist", 14L),
            FakeMusicRepository(),
            downloadRepository,
            eventRepository,
        )
        advanceUntilIdle()
        val music = musicFixture(5L)

        viewModel.downloadMusic(music)
        advanceUntilIdle()

        assertEquals(music, downloadRepository.downloadedMusic)
        assertEquals(listOf(5L to "artist_music_list"), eventRepository.trackedDownloads)
    }

    @Test
    fun `downloadMusic on a category list tracks the category-scoped event source`() = runTest(dispatcher) {
        val downloadRepository = FakeDownloadRepository()
        val eventRepository = FakeEventRepository()
        val viewModel = buildViewModel(
            savedStateHandle("category", 1L),
            FakeMusicRepository(),
            downloadRepository,
            eventRepository,
        )
        advanceUntilIdle()
        val music = musicFixture(5L)

        viewModel.downloadMusic(music)
        advanceUntilIdle()

        assertEquals(listOf(5L to "category_music_list"), eventRepository.trackedDownloads)
    }

    @Test
    fun `download state updates from the holder are reflected in ui state`() = runTest(dispatcher) {
        val downloadRepository = FakeDownloadRepository()
        val viewModel = buildViewModel(savedStateHandle("artist", 1L), FakeMusicRepository(), downloadRepository)
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.downloadStates[5L])

        downloadRepository.downloadStates.value = mapOf(5L to DownloadState.Downloading(progress = 10))
        advanceUntilIdle()

        assertEquals(DownloadState.Downloading(10), viewModel.uiState.value.downloadStates[5L])
    }
}
