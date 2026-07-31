package jp.co.studio.kaka.ui.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.domain.repository.EventRepository
import jp.co.studio.kaka.domain.repository.RecommendationRepository
import jp.co.studio.kaka.download.DownloadStateHolder
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        load(showLoading = true)
        viewModelScope.launch {
            downloadStateHolder.states.collect { states -> _uiState.update { it.copy(downloadStates = states) } }
        }
    }

    /** Pull-to-refresh. */
    fun refresh() = load(showLoading = false, isRefresh = true)

    /** "换一批" button - re-fetches without the full-screen loading spinner. */
    fun changeBatch() = load(showLoading = false)

    private fun load(showLoading: Boolean, isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = showLoading, isRefreshing = isRefresh, errorMessage = null) }
            val result = recommendationRepository.getRecommendations(
                scene = Constants.RECOMMENDATION_SCENE_RECOMMEND,
                limit = Constants.RECOMMENDATION_LIMIT_RECOMMEND,
            )
            when (result) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, recommendations = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, errorMessage = result.message)
                }
                is ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, errorMessage = "网络连接失败，请检查网络后重试")
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
