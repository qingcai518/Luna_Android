package jp.co.studio.kaka.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.model.LyricLine
import jp.co.studio.kaka.domain.repository.LyricsRepository
import jp.co.studio.kaka.player.MediaControllerRepository
import jp.co.studio.kaka.player.PlayerRepeatMode
import jp.co.studio.kaka.player.PlayerUiState
import jp.co.studio.kaka.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FullPlayerViewModel @Inject constructor(
    private val mediaControllerRepository: MediaControllerRepository,
    private val lyricsRepository: LyricsRepository,
) : ViewModel() {

    val playerState: StateFlow<PlayerUiState> = mediaControllerRepository.uiState

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()

    init {
        viewModelScope.launch {
            mediaControllerRepository.uiState
                .map { it.currentMusic?.id }
                .distinctUntilChangedBy { it }
                .collect { musicId ->
                    _lyrics.value = emptyList()
                    if (musicId != null) {
                        val result = lyricsRepository.getLyrics(musicId)
                        if (result is ApiResult.Success) _lyrics.value = result.data
                    }
                }
        }
    }

    fun playPause() = mediaControllerRepository.playPause()
    fun seekTo(positionMs: Long) = mediaControllerRepository.seekTo(positionMs)
    fun skipNext() = mediaControllerRepository.skipNext()
    fun skipPrevious() = mediaControllerRepository.skipPrevious()
    fun seekToQueueItem(index: Int) = mediaControllerRepository.seekToQueueItem(index)
    fun setRepeatMode(mode: PlayerRepeatMode) = mediaControllerRepository.setRepeatMode(mode)
    fun setShuffleEnabled(enabled: Boolean) = mediaControllerRepository.setShuffleEnabled(enabled)
}
