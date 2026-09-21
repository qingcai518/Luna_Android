package jp.co.studio.kaka.ui.musiclist

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.util.UiText

data class MusicListUiState(
    val title: String = "",
    val isLoading: Boolean = true,
    val musics: List<Music> = emptyList(),
    val errorMessage: UiText? = null,
    val downloadStates: Map<Long, DownloadState> = emptyMap(),
)
