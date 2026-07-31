package jp.co.studio.kaka.ui.downloaded

import jp.co.studio.kaka.domain.model.DownloadedMusic

data class DownloadedUiState(
    val isLoading: Boolean = true,
    val musics: List<DownloadedMusic> = emptyList(),
)
