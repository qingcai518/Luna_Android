package jp.co.studio.kaka.player

import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import jp.co.studio.kaka.di.DownloadClient
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Media3 automatically surfaces the lock-screen/notification playback controls for the
 * MediaSession created here - no manual NotificationCompat wiring needed.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    @DownloadClient
    lateinit var streamingHttpClient: OkHttpClient

    @Inject
    lateinit var playbackEventTracker: PlaybackEventTracker

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val dataSourceFactory = OkHttpDataSource.Factory(streamingHttpClient)
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        playbackEventTracker.flush()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        playbackEventTracker.flush()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
