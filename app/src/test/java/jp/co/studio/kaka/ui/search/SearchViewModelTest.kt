package jp.co.studio.kaka.ui.search

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.model.SearchResult
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.domain.repository.EventRepository
import jp.co.studio.kaka.domain.repository.SearchRepository
import jp.co.studio.kaka.download.DownloadStateHolder
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.SearchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val musicFixture = Music(
        id = 1L,
        title = "晴天",
        coverUrl = null,
        audioUrl = "https://resource.qingcai518.com/music/1.mp3",
        releaseDate = null,
        durationSeconds = null,
        artist = null,
        category = null,
    )

    private class FakeSearchRepository(private val result: ApiResult<SearchResult>) : SearchRepository {
        var searchCallCount = 0
        var lastKeyword: String? = null

        override suspend fun search(keyword: String, type: SearchType, page: Int, size: Int): ApiResult<SearchResult> {
            searchCallCount++
            lastKeyword = keyword
            return result
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
        val trackedSearchKeywords = mutableListOf<String>()
        val trackedDownloads = mutableListOf<Pair<Long, String>>()

        override suspend fun trackSkip(musicId: Long, source: String) {}

        override suspend fun trackSearch(keyword: String, source: String) {
            trackedSearchKeywords += keyword
        }

        override suspend fun trackDownload(musicId: Long, source: String) {
            trackedDownloads += musicId to source
        }

        override suspend fun trackPlay(musicId: Long, playDurationSeconds: Int, totalDurationSeconds: Int?, source: String) {}
    }

    private fun buildViewModel(
        searchRepository: SearchRepository,
        downloadRepository: DownloadRepository = FakeDownloadRepository(),
        eventRepository: EventRepository = FakeEventRepository(),
    ) = SearchViewModel(searchRepository, DownloadStateHolder(downloadRepository), eventRepository)

    @Test
    fun `keystrokes within the debounce window collapse into a single search for the final keyword`() = runTest(dispatcher) {
        val searchRepository = FakeSearchRepository(ApiResult.Success(SearchResult(musics = listOf(musicFixture))))
        val eventRepository = FakeEventRepository()
        val viewModel = buildViewModel(searchRepository, eventRepository = eventRepository)

        viewModel.onKeywordChange("周")
        advanceTimeBy(100)
        viewModel.onKeywordChange("周杰")
        advanceTimeBy(100)
        viewModel.onKeywordChange("周杰伦")
        advanceUntilIdle()

        assertEquals(1, searchRepository.searchCallCount)
        assertEquals("周杰伦", searchRepository.lastKeyword)
        assertEquals(listOf("周杰伦"), eventRepository.trackedSearchKeywords)
    }

    @Test
    fun `a keyword that stays put past the debounce window fires exactly once`() = runTest(dispatcher) {
        val searchRepository = FakeSearchRepository(ApiResult.Success(SearchResult(musics = listOf(musicFixture))))
        val viewModel = buildViewModel(searchRepository)

        viewModel.onKeywordChange("周杰伦")
        advanceUntilIdle()

        assertEquals(1, searchRepository.searchCallCount)
        assertEquals(1, viewModel.uiState.value.result.musics?.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `clearing the keyword resets results immediately without a network call`() = runTest(dispatcher) {
        val searchRepository = FakeSearchRepository(ApiResult.Success(SearchResult(musics = listOf(musicFixture))))
        val viewModel = buildViewModel(searchRepository)

        viewModel.onKeywordChange("周杰伦")
        advanceUntilIdle()
        assertEquals(1, searchRepository.searchCallCount)

        viewModel.onKeywordChange("")
        advanceUntilIdle()

        assertEquals(1, searchRepository.searchCallCount)
        assertEquals(SearchResult(), viewModel.uiState.value.result)
    }

    @Test
    fun `search error surfaces the backend message`() = runTest(dispatcher) {
        val searchRepository = FakeSearchRepository(ApiResult.Error("100010", "搜索关键词不能为空"))
        val viewModel = buildViewModel(searchRepository)

        viewModel.onKeywordChange("x")
        advanceUntilIdle()

        assertEquals("搜索关键词不能为空", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `downloadMusic delegates to the shared download state holder and tracks the event`() = runTest(dispatcher) {
        val searchRepository = FakeSearchRepository(ApiResult.Success(SearchResult()))
        val downloadRepository = FakeDownloadRepository()
        val eventRepository = FakeEventRepository()
        val viewModel = buildViewModel(searchRepository, downloadRepository, eventRepository)

        viewModel.downloadMusic(musicFixture)
        advanceUntilIdle()

        assertEquals(musicFixture, downloadRepository.downloadedMusic)
        assertEquals(listOf(musicFixture.id to "search_result"), eventRepository.trackedDownloads)
    }

    @Test
    fun `download state updates from the holder are reflected in ui state`() = runTest(dispatcher) {
        val searchRepository = FakeSearchRepository(ApiResult.Success(SearchResult()))
        val downloadRepository = FakeDownloadRepository()
        val viewModel = buildViewModel(searchRepository, downloadRepository)
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.downloadStates[musicFixture.id])

        downloadRepository.downloadStates.value = mapOf(musicFixture.id to DownloadState.Downloading(progress = 42))
        advanceUntilIdle()

        assertEquals(DownloadState.Downloading(42), viewModel.uiState.value.downloadStates[musicFixture.id])
    }
}
