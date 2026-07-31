package jp.co.studio.kaka.ui.home

import jp.co.studio.kaka.domain.model.Artist
import jp.co.studio.kaka.domain.model.Category
import jp.co.studio.kaka.domain.repository.ArtistRepository
import jp.co.studio.kaka.domain.repository.CategoryRepository
import jp.co.studio.kaka.util.ApiResult
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

    @Test
    fun `successful parallel load populates both lists`() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            FakeArtistRepository(ApiResult.Success(listOf(artistFixture))),
            FakeCategoryRepository(ApiResult.Success(listOf(categoryFixture))),
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
        val viewModel = HomeViewModel(
            FakeArtistRepository(ApiResult.Error("100020", "获取歌手列表失败")),
            FakeCategoryRepository(ApiResult.Success(listOf(categoryFixture))),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.artists.isEmpty())
        assertEquals(listOf(categoryFixture), state.categories)
        assertEquals("获取歌手列表失败", state.errorMessage)
    }

    @Test
    fun `network error on either call surfaces the generic connectivity message`() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            FakeArtistRepository(ApiResult.NetworkError(IOException("timeout"))),
            FakeCategoryRepository(ApiResult.Success(listOf(categoryFixture))),
        )
        advanceUntilIdle()

        assertEquals("网络连接失败，请检查网络后重试", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `load can be re-triggered manually and refreshes the state`() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            FakeArtistRepository(ApiResult.Success(listOf(artistFixture))),
            FakeCategoryRepository(ApiResult.Success(emptyList())),
        )
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.artists.size)

        viewModel.load()
        advanceUntilIdle()

        assertEquals(listOf(artistFixture), viewModel.uiState.value.artists)
    }
}
