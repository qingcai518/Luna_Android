package jp.co.studio.kaka.ui.recommend

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Recommendation

data class RecommendUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val recommendations: List<Recommendation> = emptyList(),
    val errorMessage: String? = null,
    val downloadStates: Map<Long, DownloadState> = emptyMap(),
)
