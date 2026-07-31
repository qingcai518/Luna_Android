package jp.co.studio.kaka.ui.player

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.player.MediaControllerRepository
import jp.co.studio.kaka.player.PlayerRepeatMode
import jp.co.studio.kaka.player.PlayerUiState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin Compose-facing wrapper over [MediaControllerRepository]. MainScaffold holds a single
 * instance and passes it to both MiniPlayerBar and FullPlayerScreen so they share one state.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val mediaControllerRepository: MediaControllerRepository,
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = mediaControllerRepository.uiState

    fun playQueue(musics: List<Music>, startIndex: Int) = mediaControllerRepository.playQueue(musics, startIndex)
    fun playPause() = mediaControllerRepository.playPause()
    fun seekTo(positionMs: Long) = mediaControllerRepository.seekTo(positionMs)
    fun skipNext() = mediaControllerRepository.skipNext()
    fun skipPrevious() = mediaControllerRepository.skipPrevious()
    fun seekToQueueItem(index: Int) = mediaControllerRepository.seekToQueueItem(index)
    fun setRepeatMode(mode: PlayerRepeatMode) = mediaControllerRepository.setRepeatMode(mode)
    fun setShuffleEnabled(enabled: Boolean) = mediaControllerRepository.setShuffleEnabled(enabled)
    fun stopAndClear() = mediaControllerRepository.stopAndClear()
}
