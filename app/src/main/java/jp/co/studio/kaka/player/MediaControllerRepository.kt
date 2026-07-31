package jp.co.studio.kaka.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.co.studio.kaka.domain.model.Music
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-level bridge between the [MediaController] connected to [PlaybackService] and a
 * single [PlayerUiState] StateFlow. MainScaffold holds one PlayerViewModel wrapping this
 * repository and passes it explicitly to both MiniPlayerBar and FullPlayerScreen, so the two
 * always read the same state and never drift out of sync.
 */
@Singleton
class MediaControllerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val queueBuilder: QueueBuilder,
    private val autoSkipHandler: AutoSkipHandler,
    private val playbackEventTracker: PlaybackEventTracker,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controller: MediaController? = null
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
                attachListener()
            },
            MoreExecutors.directExecutor(),
        )
    }

    private fun attachListener() {
        val player = controller ?: return
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                playbackEventTracker.onPlayingChanged(isPlaying)
                if (isPlaying) startProgressTicker() else stopProgressTicker()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = player.currentMediaItemIndex
                _uiState.update { it.copy(currentIndex = index, durationMs = player.duration.coerceAtLeast(0)) }
                playbackEventTracker.onTrackChanged(_uiState.value.queue.getOrNull(index))
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val mapped = when (playbackState) {
                    Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                    Player.STATE_READY -> PlaybackState.READY
                    Player.STATE_ENDED -> PlaybackState.ENDED
                    else -> PlaybackState.IDLE
                }
                _uiState.update { it.copy(playbackState = mapped, durationMs = player.duration.coerceAtLeast(0)) }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _uiState.update { it.copy(repeatMode = repeatMode.toPlayerRepeatMode()) }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _uiState.update { it.copy(shuffleEnabled = shuffleModeEnabled) }
            }

            override fun onPlayerError(error: PlaybackException) {
                val failedIndex = player.currentMediaItemIndex
                val nextIndex = autoSkipHandler.onItemFailed(failedIndex)
                if (nextIndex == null) {
                    player.pause()
                } else {
                    player.seekTo(nextIndex, 0L)
                    player.play()
                }
            }
        })
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        progressJob = scope.launch {
            while (isActive) {
                controller?.let { player -> _uiState.update { it.copy(positionMs = player.currentPosition.coerceAtLeast(0)) } }
                playbackEventTracker.tick()
                delay(200)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun playQueue(musics: List<Music>, startIndex: Int) {
        val player = controller ?: return
        autoSkipHandler.reset(musics.size)
        _uiState.update { it.copy(queue = musics, currentIndex = startIndex) }
        playbackEventTracker.onTrackChanged(musics.getOrNull(startIndex))
        scope.launch {
            val mediaItems = queueBuilder.buildMediaItems(musics)
            player.setMediaItems(mediaItems, startIndex, 0L)
            player.prepare()
            player.play()
        }
    }

    fun playPause() {
        val player = controller ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun skipNext() {
        controller?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        controller?.seekToPreviousMediaItem()
    }

    fun seekToQueueItem(index: Int) {
        controller?.seekTo(index, 0L)
    }

    fun setRepeatMode(mode: PlayerRepeatMode) {
        controller?.repeatMode = mode.toExoRepeatMode()
    }

    fun setShuffleEnabled(enabled: Boolean) {
        controller?.shuffleModeEnabled = enabled
    }

    /** Called on logout (both user-initiated and forced) - playback must not survive a session end. */
    fun stopAndClear() {
        playbackEventTracker.flush()
        controller?.stop()
        controller?.clearMediaItems()
        _uiState.value = PlayerUiState()
    }
}
