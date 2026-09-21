package jp.co.studio.kaka.ui.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.repository.EventRepository
import jp.co.studio.kaka.domain.repository.RecommendationRepository
import jp.co.studio.kaka.download.DownloadStateHolder
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.Constants
import jp.co.studio.kaka.util.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val downloadStateHolder: DownloadStateHolder,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendUiState())
    val uiState: StateFlow<RecommendUiState> = _uiState.asStateFlow()

    /** 一次性的提示（如「暂时没有新的推荐」），界面用 Toast 显示。 */
    private val _events = MutableSharedFlow<UiText>(extraBufferCapacity = 1)
    val events: SharedFlow<UiText> = _events.asSharedFlow()

    init {
        load(showLoading = true)
        viewModelScope.launch {
            downloadStateHolder.states.collect { states -> _uiState.update { it.copy(downloadStates = states) } }
        }
    }

    /** Pull-to-refresh. */
    fun refresh() = load(showLoading = false, isRefresh = true)

    /** "换一批" button - re-fetches without the full-screen loading spinner. */
    fun changeBatch() = load(showLoading = false, isChangeBatch = true)

    private fun load(showLoading: Boolean, isRefresh: Boolean = false, isChangeBatch: Boolean = false) {
        viewModelScope.launch {
            val previousIds = _uiState.value.recommendations.map { it.music.id }
            _uiState.update {
                it.copy(isLoading = showLoading, isRefreshing = isRefresh, isChangingBatch = isChangeBatch, errorMessage = null)
            }
            val result = recommendationRepository.getRecommendations(
                scene = Constants.RECOMMENDATION_SCENE_RECOMMEND,
                limit = Constants.RECOMMENDATION_LIMIT_RECOMMEND,
            )
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, isChangingBatch = false, recommendations = result.data)
                    }
                    // 手动换一批却拿到完全相同的一批：后端把推荐缓存了一段时间，如实告诉用户，
                    // 而不是让「换一批」看起来没反应。
                    val manual = isRefresh || isChangeBatch
                    if (manual && previousIds.isNotEmpty() && result.data.map { it.music.id } == previousIds) {
                        _events.tryEmit(UiText.Resource(R.string.recommend_no_new))
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, isChangingBatch = false, errorMessage = UiText.Dynamic(result.message))
                }
                is ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, isChangingBatch = false, errorMessage = UiText.Resource(R.string.error_network))
                }
            }
        }
    }

    /** Swipe-to-dismiss "not interested" - local removal + SKIP event only, no server-side preference change (matches iOS). */
    fun dismiss(recommendation: Recommendation) {
        _uiState.update { state ->
            state.copy(recommendations = state.recommendations.filterNot { it.music.id == recommendation.music.id })
        }
        viewModelScope.launch { eventRepository.trackSkip(recommendation.music.id, source = "recommend_page") }
    }

    fun downloadMusic(music: Music) {
        downloadStateHolder.download(music)
        viewModelScope.launch { eventRepository.trackDownload(music.id, source = "recommend_page") }
    }
}
