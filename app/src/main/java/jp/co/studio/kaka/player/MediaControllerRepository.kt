package jp.co.studio.kaka.player

import jp.co.studio.kaka.domain.model.Music
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridge between the [androidx.media3.session.MediaController] connected to [PlaybackService] and
 * a single [PlayerUiState] StateFlow. MainScaffold holds one PlayerViewModel wrapping this and
 * passes it explicitly to both MiniPlayerBar and FullPlayerScreen, so the two always read the
 * same state and never drift out of sync.
 *
 * Extracted as an interface (implemented by [MediaControllerRepositoryImpl]) purely so
 * ViewModels that depend on it - ProfileViewModel, DownloadedViewModel, PlayerViewModel,
 * FullPlayerViewModel - can be unit tested against a fake instead of a real Media3 controller.
 */
interface MediaControllerRepository {
    val uiState: StateFlow<PlayerUiState>

    fun playQueue(musics: List<Music>, startIndex: Int)
    fun playPause()
    fun seekTo(positionMs: Long)
    fun skipNext()
    fun skipPrevious()
    fun seekToQueueItem(index: Int)
    fun setRepeatMode(mode: PlayerRepeatMode)
    fun setShuffleEnabled(enabled: Boolean)

    /** Called on logout (both user-initiated and forced) - playback must not survive a session end. */
    fun stopAndClear()
}
