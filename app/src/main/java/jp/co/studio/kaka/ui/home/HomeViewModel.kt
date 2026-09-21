package jp.co.studio.kaka.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.repository.ArtistRepository
import jp.co.studio.kaka.domain.repository.CategoryRepository
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.domain.repository.RecommendationRepository
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.Constants
import jp.co.studio.kaka.util.UiText
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val categoryRepository: CategoryRepository,
    private val recommendationRepository: RecommendationRepository,
    downloadRepository: DownloadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
        loadHero()
        viewModelScope.launch {
            downloadRepository.downloadedMusics.collect { musics ->
                _uiState.update { it.copy(downloadedCount = musics.size) }
            }
        }
    }

    /** 重试：列表和主角卡都重新加载。 */
    fun retry() {
        load()
        loadHero()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            coroutineScope {
                val artistsDeferred = async { artistRepository.getArtists() }
                val categoriesDeferred = async { categoryRepository.getCategories() }
                val artistsResult = artistsDeferred.await()
                val categoriesResult = categoriesDeferred.await()

                val artists = (artistsResult as? ApiResult.Success)?.data.orEmpty()
                val categories = (categoriesResult as? ApiResult.Success)?.data.orEmpty()
                val errorMessage = when {
                    artistsResult is ApiResult.Error -> UiText.Dynamic(artistsResult.message)
                    categoriesResult is ApiResult.Error -> UiText.Dynamic(categoriesResult.message)
                    artistsResult is ApiResult.NetworkError || categoriesResult is ApiResult.NetworkError ->
                        UiText.Resource(R.string.error_network)
                    else -> null
                }
                _uiState.update {
                    it.copy(isLoading = false, artists = artists, categories = categories, errorMessage = errorMessage)
                }
            }
        }
    }

    /**
     * 主角卡必须和艺术家/分类分开加载：后端 `/recommendations?scene=home` 在缓存未命中时会同步调用大模型，
     * 可能慢到几秒甚至几十秒。这里独立请求并设超时：超时 / 失败 / 没有推荐都只是不显示主角卡，不报错、不阻塞页面。
     */
    private fun loadHero() {
        viewModelScope.launch {
            _uiState.update { it.copy(isHeroLoading = true) }
            val result = withTimeoutOrNull(HERO_TIMEOUT_MS) {
                recommendationRepository.getRecommendations(
                    scene = Constants.RECOMMENDATION_SCENE_HOME,
                    limit = Constants.RECOMMENDATION_LIMIT_HOME,
                )
            }
            val items = (result as? ApiResult.Success)?.data.orEmpty()
            _uiState.update { it.copy(isHeroLoading = false, heroItems = items) }
        }
    }

    private companion object {
        const val HERO_TIMEOUT_MS = 6_000L
    }
}
