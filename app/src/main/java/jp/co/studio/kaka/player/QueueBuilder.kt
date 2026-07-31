package jp.co.studio.kaka.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import jp.co.studio.kaka.data.local.db.dao.DownloadedMusicDao
import jp.co.studio.kaka.domain.model.Music
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds ExoPlayer [MediaItem]s from [Music], checking [DownloadedMusicDao] first so a queue can
 * mix network streams and already-downloaded local files transparently. Downloaded entries always
 * use their local cover file too - never the possibly-expired (>1h) remote signed cover URL.
 */
@Singleton
class QueueBuilder @Inject constructor(
    private val downloadedMusicDao: DownloadedMusicDao,
) {

    suspend fun buildMediaItem(music: Music): MediaItem {
        val downloaded = downloadedMusicDao.getById(music.id)
        val audioUri = downloaded?.localAudioPath?.let { Uri.fromFile(File(it)) } ?: Uri.parse(music.audioUrl)
        val artworkUri = downloaded?.localCoverPath?.let { Uri.fromFile(File(it)) }
            ?: music.coverUrl?.let(Uri::parse)

        return MediaItem.Builder()
            .setMediaId(music.id.toString())
            .setUri(audioUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(music.title)
                    .setArtist(music.artist?.name)
                    .apply { artworkUri?.let { setArtworkUri(it) } }
                    .build(),
            )
            .build()
    }

    suspend fun buildMediaItems(musics: List<Music>): List<MediaItem> = musics.map { buildMediaItem(it) }
}
