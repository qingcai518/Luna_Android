package jp.co.studio.kaka.ui.musiclist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.repository.EventRepository
import jp.co.studio.kaka.domain.repository.MusicRepository
import jp.co.studio.kaka.download.DownloadStateHolder
import jp.co.studio.kaka.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class MusicListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val downloadStateHolder: DownloadStateHolder,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val idType: String = savedStateHandle.get<String>("idType").orEmpty()
    private val id: Long = savedStateHandle.get<Long>("id") ?: 0L
    private val name: String = savedStateHandle.get<String>("name")
        ?.let { URLDecoder.decode(it, "UTF-8") }
        .orEmpty()

    private val eventSource: String get() = if (idType == "artist") "artist_music_list" else "category_music_list"

    private val _uiState = MutableStateFlow(MusicListUiState(title = name))
    val uiState: StateFlow<MusicListUiState> = _uiState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            downloadStateHolder.states.collect { states -> _uiState.update { it.copy(downloadStates = states) } }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (idType == "artist") {
                musicRepository.getMusicByArtist(id)
            } else {
                musicRepository.getMusicByCategory(id)
            }
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, musics = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "网络连接失败，请检查网络后重试")
                }
            }
        }
    }

    fun downloadMusic(music: Music) {
        downloadStateHolder.download(music)
        viewModelScope.launch { eventRepository.trackDownload(music.id, source = eventSource) }
    }
}
