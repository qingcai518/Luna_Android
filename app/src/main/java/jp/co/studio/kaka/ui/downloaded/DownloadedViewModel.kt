package jp.co.studio.kaka.ui.downloaded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.toMusic
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.player.MediaControllerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadedViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val mediaControllerRepository: MediaControllerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadedUiState())
    val uiState: StateFlow<DownloadedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { downloadRepository.reconcile() }
        viewModelScope.launch {
            downloadRepository.downloadedMusics.collect { musics ->
                _uiState.update { it.copy(isLoading = false, musics = musics) }
            }
        }
    }

    fun play(musics: List<DownloadedMusic>, index: Int) {
        mediaControllerRepository.playQueue(musics.map { it.toMusic() }, index)
    }

    /** If the item being deleted is currently playing, skip to the next track first. */
    fun delete(musicId: Long) {
        viewModelScope.launch {
            if (mediaControllerRepository.uiState.value.currentMusic?.id == musicId) {
                mediaControllerRepository.skipNext()
            }
            downloadRepository.delete(musicId)
        }
    }
}
