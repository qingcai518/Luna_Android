package jp.co.studio.kaka.ui.recommend

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.util.UiText

data class RecommendUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** 点「换一批」按钮触发的重新拉取（下拉刷新是 [isRefreshing]）：只让头部按钮转圈，列表保持原样。 */
    val isChangingBatch: Boolean = false,
    val recommendations: List<Recommendation> = emptyList(),
    val errorMessage: UiText? = null,
    val downloadStates: Map<Long, DownloadState> = emptyMap(),
)
