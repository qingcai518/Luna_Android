package jp.co.studio.kaka.ui.home

import jp.co.studio.kaka.domain.model.Artist
import jp.co.studio.kaka.domain.model.Category

data class HomeUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
)
