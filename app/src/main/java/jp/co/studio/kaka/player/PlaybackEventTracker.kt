package jp.co.studio.kaka.player

import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.repository.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks actual accumulated listening seconds per track (not just "playback started") and
 * reports a PLAY event on track change / pause / stop - mirrors the iOS design where a "play"
 * event means real elapsed listening time.
 */
@Singleton
class PlaybackEventTracker @Inject constructor(
    private val eventRepository: EventRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var currentMusic: Music? = null
    private var accumulatedSeconds = 0
    private var lastTickAtMs = 0L
    private var isPlaying = false

    fun onTrackChanged(music: Music?) {
        flush()
        currentMusic = music
        accumulatedSeconds = 0
        lastTickAtMs = System.currentTimeMillis()
    }

    fun onPlayingChanged(playing: Boolean) {
        tick()
        isPlaying = playing
        lastTickAtMs = System.currentTimeMillis()
        if (!playing) flush()
    }

    /** Call periodically while playing (the existing 200ms progress ticker is a natural hook). */
    fun tick() {
        if (!isPlaying) return
        val now = System.currentTimeMillis()
        accumulatedSeconds += ((now - lastTickAtMs) / 1000).toInt()
        lastTickAtMs = now
    }

    /** Flush on pause/track-change/stop and also from PlaybackService.onDestroy/onTaskRemoved. */
    fun flush() {
        tick()
        val music = currentMusic
        val seconds = accumulatedSeconds
        if (music != null && seconds > 0) {
            scope.launch { eventRepository.trackPlay(music.id, seconds, music.durationSeconds, source = "player") }
        }
        accumulatedSeconds = 0
    }
}
