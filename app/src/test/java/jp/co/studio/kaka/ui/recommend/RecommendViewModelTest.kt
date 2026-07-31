package jp.co.studio.kaka.ui.recommend

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.domain.repository.EventRepository
import jp.co.studio.kaka.domain.repository.RecommendationRepository
import jp.co.studio.kaka.download.DownloadStateHolder
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendViewModelTest {

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

    private fun recommendationFixture(id: Long, reason: String = "热门推荐") =
        Recommendation(music = musicFixture(id), score = 0.8, reason = reason)

    /** [delayMs] lets tests observe the in-flight (isLoading/isRefreshing) state before completion. */
    private class FakeRecommendationRepository(
        private val results: List<ApiResult<List<Recommendation>>>,
        private val delayMs: Long = 0,
    ) : RecommendationRepository {
        constructor(result: ApiResult<List<Recommendation>>, delayMs: Long = 0) : this(listOf(result), delayMs)

        var callCount = 0
        var lastScene: String? = null
        var lastLimit: Int? = null

        override suspend fun getRecommendations(scene: String, limit: Int): ApiResult<List<Recommendation>> {
            if (delayMs > 0) delay(delayMs)
            lastScene = scene
            lastLimit = limit
            val result = results.getOrElse(callCount) { results.last() }
            callCount++
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
        val trackedSkips = mutableListOf<Pair<Long, String>>()
        val trackedDownloads = mutableListOf<Pair<Long, String>>()

        override suspend fun trackSkip(musicId: Long, source: String) {
            trackedSkips += musicId to source
        }

        override suspend fun trackSearch(keyword: String, source: String) {}

        override suspend fun trackDownload(musicId: Long, source: String) {
            trackedDownloads += musicId to source
        }

        override suspend fun trackPlay(musicId: Long, playDurationSeconds: Int, totalDurationSeconds: Int?, source: String) {}
    }

    private fun buildViewModel(
        recommendationRepository: RecommendationRepository,
        downloadRepository: DownloadRepository = FakeDownloadRepository(),
        eventRepository: EventRepository = FakeEventRepository(),
    ) = RecommendViewModel(recommendationRepository, DownloadStateHolder(downloadRepository), eventRepository)

    @Test
    fun `initial load requests the recommend scene with the recommend-page limit`() = runTest(dispatcher) {
        val repository = FakeRecommendationRepository(ApiResult.Success(listOf(recommendationFixture(1L))))
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        assertEquals(Constants.RECOMMENDATION_SCENE_RECOMMEND, repository.lastScene)
        assertEquals(Constants.RECOMMENDATION_LIMIT_RECOMMEND, repository.lastLimit)
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.recommendations.size)
        assertEquals("热门推荐", state.recommendations.first().reason)
    }

    @Test
    fun `initial load shows the full-screen spinner while in flight, then clears it`() = runTest(dispatcher) {
        val repository = FakeRecommendationRepository(ApiResult.Success(listOf(recommendationFixture(1L))), delayMs = 100)
        val viewModel = buildViewModel(repository)
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.recommendations.size)
    }

    @Test
    fun `refresh flips isRefreshing without the full-screen spinner while in flight, then clears it`() = runTest(dispatcher) {
        val repository = FakeRecommendationRepository(ApiResult.Success(listOf(recommendationFixture(1L))), delayMs = 100)
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.refresh()
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isRefreshing)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `changeBatch re-fetches without the full-screen spinner and replaces the list`() = runTest(dispatcher) {
        val repository = FakeRecommendationRepository(
            listOf(
                ApiResult.Success(listOf(recommendationFixture(1L))),
                ApiResult.Success(listOf(recommendationFixture(2L))),
            ),
            delayMs = 100,
        )
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.changeBatch()
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isRefreshing)

        advanceUntilIdle()

        assertEquals(2, repository.callCount)
        assertEquals(2L, viewModel.uiState.value.recommendations.first().music.id)
    }

    @Test
    fun `error surfaces the backend message and clears loading and refreshing flags`() = runTest(dispatcher) {
        val repository = FakeRecommendationRepository(ApiResult.Error("100040", "获取推荐失败"))
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("获取推荐失败", state.errorMessage)
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `network error surfaces the generic connectivity message`() = runTest(dispatcher) {
        val repository = FakeRecommendationRepository(ApiResult.NetworkError(IOException("timeout")))
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        assertEquals("网络连接失败，请检查网络后重试", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `dismiss removes the item locally and reports a skip event, without a follow-up fetch`() = runTest(dispatcher) {
        val repository = FakeRecommendationRepository(
            ApiResult.Success(listOf(recommendationFixture(1L), recommendationFixture(2L))),
        )
        val eventRepository = FakeEventRepository()
        val viewModel = buildViewModel(repository, eventRepository = eventRepository)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.recommendations.size)

        viewModel.dismiss(recommendationFixture(1L))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.recommendations.size)
        assertEquals(2L, state.recommendations.first().music.id)
        assertEquals(listOf(1L to "recommend_page"), eventRepository.trackedSkips)
        assertEquals(1, repository.callCount)
    }

    @Test
    fun `downloadMusic delegates to the shared download state holder and tracks the event`() = runTest(dispatcher) {
        val repository = FakeRecommendationRepository(ApiResult.Success(emptyList()))
        val downloadRepository = FakeDownloadRepository()
        val eventRepository = FakeEventRepository()
        val viewModel = buildViewModel(repository, downloadRepository, eventRepository)
        advanceUntilIdle()
        val music = musicFixture(5L)

        viewModel.downloadMusic(music)
        advanceUntilIdle()

        assertEquals(music, downloadRepository.downloadedMusic)
        assertEquals(listOf(5L to "recommend_page"), eventRepository.trackedDownloads)
    }
}
