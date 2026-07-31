package jp.co.studio.kaka.player

import androidx.media3.common.Player
import jp.co.studio.kaka.domain.model.Music

enum class PlayerRepeatMode { OFF, ONE, ALL }

enum class PlaybackState { IDLE, BUFFERING, READY, ENDED }

data class PlayerUiState(
    val queue: List<Music> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: PlayerRepeatMode = PlayerRepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val playbackState: PlaybackState = PlaybackState.IDLE,
) {
    val currentMusic: Music? get() = queue.getOrNull(currentIndex)
}

fun Int.toPlayerRepeatMode(): PlayerRepeatMode = when (this) {
    Player.REPEAT_MODE_ONE -> PlayerRepeatMode.ONE
    Player.REPEAT_MODE_ALL -> PlayerRepeatMode.ALL
    else -> PlayerRepeatMode.OFF
}

fun PlayerRepeatMode.toExoRepeatMode(): Int = when (this) {
    PlayerRepeatMode.OFF -> Player.REPEAT_MODE_OFF
    PlayerRepeatMode.ONE -> Player.REPEAT_MODE_ONE
    PlayerRepeatMode.ALL -> Player.REPEAT_MODE_ALL
}
