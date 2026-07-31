package jp.co.studio.kaka.ui.search

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.SearchResult

data class SearchUiState(
    val keyword: String = "",
    val isLoading: Boolean = false,
    val result: SearchResult = SearchResult(),
    val errorMessage: String? = null,
    val downloadStates: Map<Long, DownloadState> = emptyMap(),
) {
    val isEmpty: Boolean
        get() = keyword.isNotBlank() && !isLoading && errorMessage == null &&
            result.musics.isNullOrEmpty() && result.artists.isNullOrEmpty() &&
            result.categories.isNullOrEmpty() && result.regions.isNullOrEmpty()
}
