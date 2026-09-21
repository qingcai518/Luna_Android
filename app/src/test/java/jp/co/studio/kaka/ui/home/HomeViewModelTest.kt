package jp.co.studio.kaka.ui.home

import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.Artist
import jp.co.studio.kaka.domain.model.Category
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.domain.repository.ArtistRepository
import jp.co.studio.kaka.domain.repository.CategoryRepository
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.domain.repository.RecommendationRepository
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val artistFixture = Artist(id = 14L, name = "王菲", regionCode = null, bio = null, avatarUrl = null)
    private val categoryFixture = Category(id = 1L, name = "流行", coverUrl = null, description = null)

    private class FakeArtistRepository(private val result: ApiResult<List<Artist>>) : ArtistRepository {
        override suspend fun getArtists(): ApiResult<List<Artist>> = result
    }

    private class FakeCategoryRepository(private val result: ApiResult<List<Category>>) : CategoryRepository {
        override suspend fun getCategories(): ApiResult<List<Category>> = result
    }

    private class FakeRecommendationRepository(
        private val result: ApiResult<List<Recommendation>> = ApiResult.Success(emptyList()),
        private val delayMs: Long = 0L,
    ) : RecommendationRepository {
        override suspend fun getRecommendations(scene: String, limit: Int): ApiResult<List<Recommendation>> {
            if (delayMs > 0) delay(delayMs)
            return result
        }
    }

    private class FakeDownloadRepository(count: Int = 0) : DownloadRepository {
        override val downloadStates: StateFlow<Map<Long, DownloadState>> = MutableStateFlow(emptyMap())
        override val downloadedMusics: Flow<List<DownloadedMusic>> = flowOf(
            List(count) { i ->
                DownloadedMusic(
                    id = i.toLong(), title = "t$i", localCoverPath = null, localAudioPath = "/x/$i.mp3", localLyricsPath = null,
                    releaseDate = null, durationSeconds = null, artistName = "a", categoryName = "c", downloadDate = 0L,
                )
            },
        )
        override fun download(music: Music) = Unit
        override suspend fun delete(musicId: Long) = Unit
        override suspend fun reconcile() = Unit
    }

    private fun recommendation(id: Long) = Recommendation(
        music = Music(id, "曲目$id", null, "https://x/$id.mp3", null, null, null, null),
        score = 0.9,
        reason = "夜深了，来一首安静的",
    )

    private fun newViewModel(
        artists: ApiResult<List<Artist>>,
        categories: ApiResult<List<Category>>,
        recommendations: FakeRecommendationRepository = FakeRecommendationRepository(),
        downloads: FakeDownloadRepository = FakeDownloadRepository(),
    ) = HomeViewModel(FakeArtistRepository(artists), FakeCategoryRepository(categories), recommendations, downloads)

    @Test
    fun `successful parallel load populates both lists`() = runTest(dispatcher) {
        val viewModel = newViewModel(
            ApiResult.Success(listOf(artistFixture)),
            ApiResult.Success(listOf(categoryFixture)),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf(artistFixture), state.artists)
        assertEquals(listOf(categoryFixture), state.categories)
        assertNull(state.errorMessage)
    }

    @Test
    fun `artist failure still surfaces categories that loaded successfully`() = runTest(dispatcher) {
        // HomeViewModel fires both requests in parallel via async - one failing must not
        // discard the other's result, only surface its error message.
        val viewModel = newViewModel(
            ApiResult.Error("100020", "获取歌手列表失败"),
            ApiResult.Success(listOf(categoryFixture)),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.artists.isEmpty())
        assertEquals(listOf(categoryFixture), state.categories)
        assertEquals(UiText.Dynamic("获取歌手列表失败"), state.errorMessage)
    }

    @Test
    fun `network error on either call surfaces the generic connectivity message`() = runTest(dispatcher) {
        val viewModel = newViewModel(
            ApiResult.NetworkError(IOException("timeout")),
            ApiResult.Success(listOf(categoryFixture)),
        )
        advanceUntilIdle()

        assertEquals(UiText.Resource(R.string.error_network), viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `load can be re-triggered manually and refreshes the state`() = runTest(dispatcher) {
        val viewModel = newViewModel(
            ApiResult.Success(listOf(artistFixture)),
            ApiResult.Success(emptyList()),
        )
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.artists.size)

        viewModel.load()
        advanceUntilIdle()

        assertEquals(listOf(artistFixture), viewModel.uiState.value.artists)
    }

    @Test
    fun `hero recommendations load independently of the lists and expose the first one`() = runTest(dispatcher) {
        val viewModel = newViewModel(
            ApiResult.Success(listOf(artistFixture)),
            ApiResult.Success(listOf(categoryFixture)),
            recommendations = FakeRecommendationRepository(ApiResult.Success(listOf(recommendation(1), recommendation(2)))),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isHeroLoading)
        assertEquals(2, state.heroItems.size)
        assertEquals(1L, state.hero?.music?.id)
        // 主角卡加载不影响艺术家/分类
        assertEquals(listOf(artistFixture), state.artists)
    }

    @Test
    fun `hero failure is silent - no error message and the lists still load`() = runTest(dispatcher) {
        val viewModel = newViewModel(
            ApiResult.Success(listOf(artistFixture)),
            ApiResult.Success(listOf(categoryFixture)),
            recommendations = FakeRecommendationRepository(ApiResult.Error("401", "unauthorized")),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isHeroLoading)
        assertTrue(state.heroItems.isEmpty())
        assertNull(state.errorMessage)
        assertEquals(listOf(categoryFixture), state.categories)
    }

    @Test
    fun `a slow hero request times out instead of blocking - lists are shown and hero is hidden`() = runTest(dispatcher) {
        // 后端 AI 推荐缓存未命中时会等大模型，可能非常慢：主角卡最多等 6 秒，之后直接放弃
        val viewModel = newViewModel(
            ApiResult.Success(listOf(artistFixture)),
            ApiResult.Success(listOf(categoryFixture)),
            recommendations = FakeRecommendationRepository(ApiResult.Success(listOf(recommendation(1))), delayMs = 60_000L),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isHeroLoading)
        assertTrue(state.heroItems.isEmpty())
        assertEquals(listOf(artistFixture), state.artists)
    }

    @Test
    fun `downloaded count follows the download repository`() = runTest(dispatcher) {
        val viewModel = newViewModel(
            ApiResult.Success(emptyList()),
            ApiResult.Success(emptyList()),
            downloads = FakeDownloadRepository(count = 3),
        )
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.downloadedCount)
    }
}
