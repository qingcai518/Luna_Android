package jp.co.studio.kaka.data.local.files

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.co.studio.kaka.data.local.db.dao.DownloadedMusicDao
import jp.co.studio.kaka.data.local.db.entity.DownloadedMusicEntity
import jp.co.studio.kaka.util.Constants
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: DownloadedMusicDao,
) {
    fun musicDir(): File = File(context.filesDir, Constants.DOWNLOAD_DIR_MUSIC).apply { mkdirs() }
    fun coversDir(): File = File(context.filesDir, Constants.DOWNLOAD_DIR_COVERS).apply { mkdirs() }
    fun lyricsDir(): File = File(context.filesDir, Constants.DOWNLOAD_DIR_LYRICS).apply { mkdirs() }

    fun audioFile(musicId: Long): File = File(musicDir(), "$musicId.mp3")
    fun coverFile(musicId: Long): File = File(coversDir(), "$musicId.jpg")
    fun lyricsFile(musicId: Long): File = File(lyricsDir(), "$musicId.lrc")

    /**
     * File system <-> database consistency self-heal, mirroring the iOS DLMusicModel logic:
     * drop DB rows whose audio file has vanished, and reverse-register orphan audio files the
     * DB doesn't know about (e.g. after a DB rebuild) using the filename as a fallback title.
     */
    suspend fun reconcile() {
        val entities = dao.getAllOnce()
        entities.forEach { entity ->
            if (!File(entity.localAudioPath).exists()) {
                dao.deleteById(entity.id)
            }
        }

        val knownIds = entities.mapNotNull { entity -> if (File(entity.localAudioPath).exists()) entity.id else null }.toSet()
        val audioFiles = musicDir().listFiles { file -> file.extension.equals("mp3", ignoreCase = true) }.orEmpty()
        audioFiles.forEach { file ->
            val id = file.nameWithoutExtension.toLongOrNull() ?: return@forEach
            if (id in knownIds) return@forEach
            dao.upsert(
                DownloadedMusicEntity(
                    id = id,
                    title = file.nameWithoutExtension,
                    localCoverPath = coverFile(id).takeIf { it.exists() }?.absolutePath,
                    localAudioPath = file.absolutePath,
                    localLyricsPath = lyricsFile(id).takeIf { it.exists() }?.absolutePath,
                    releaseDate = null,
                    durationSeconds = null,
                    artistName = "Unknown Artist",
                    categoryName = "Unknown",
                    downloadDate = file.lastModified(),
                ),
            )
        }
    }
}
