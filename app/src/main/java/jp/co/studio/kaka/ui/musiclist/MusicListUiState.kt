package jp.co.studio.kaka.ui.musiclist

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Music

data class MusicListUiState(
    val title: String = "",
    val isLoading: Boolean = true,
    val musics: List<Music> = emptyList(),
    val errorMessage: String? = null,
    val downloadStates: Map<Long, DownloadState> = emptyMap(),
)
