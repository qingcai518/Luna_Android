package jp.co.studio.kaka.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import jp.co.studio.kaka.data.local.db.dao.DownloadedMusicDao
import jp.co.studio.kaka.data.local.db.entity.DownloadedMusicEntity
import jp.co.studio.kaka.data.local.files.DownloadFileManager
import jp.co.studio.kaka.data.remote.api.ContentApiService
import jp.co.studio.kaka.di.DownloadClient
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Audio download is required (failure fails the whole task); cover and lyrics are best-effort
 * and never fail the overall download - mirrors the iOS "download audio -> try cover -> try
 * lyrics" chain. Lyrics require their own network round-trip (GET /lyrics) since the signed
 * lyricUrl isn't known until the audio is already being fetched.
 */
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    @DownloadClient private val httpClient: OkHttpClient,
    private val contentApiService: ContentApiService,
    private val json: Json,
    private val fileManager: DownloadFileManager,
    private val dao: DownloadedMusicDao,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val musicId = inputData.getLong(KEY_MUSIC_ID, -1L)
        val audioUrl = inputData.getString(KEY_AUDIO_URL)
        if (musicId == -1L || audioUrl == null) return@withContext Result.failure()

        val coverUrl = inputData.getString(KEY_COVER_URL)
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val artistName = inputData.getString(KEY_ARTIST_NAME) ?: "Unknown Artist"
        val categoryName = inputData.getString(KEY_CATEGORY_NAME) ?: "Unknown"
        val releaseDate = inputData.getString(KEY_RELEASE_DATE)
        val durationSeconds = inputData.getInt(KEY_DURATION_SECONDS, -1).takeIf { it >= 0 }

        val audioFile = fileManager.audioFile(musicId)
        val audioOk = downloadToFile(audioUrl, audioFile) { progress ->
            setProgressAsync(workDataOf(KEY_PROGRESS to progress))
        }
        if (!audioOk) return@withContext Result.failure()

        val coverFile = coverUrl?.let { url ->
            fileManager.coverFile(musicId).takeIf { downloadToFile(url, it) }
        }
        val lyricsFile = fetchLyricUrl(musicId)?.let { url ->
            fileManager.lyricsFile(musicId).takeIf { downloadToFile(url, it) }
        }

        dao.upsert(
            DownloadedMusicEntity(
                id = musicId,
                title = title,
                localCoverPath = coverFile?.absolutePath,
                localAudioPath = audioFile.absolutePath,
                localLyricsPath = lyricsFile?.absolutePath,
                releaseDate = releaseDate,
                durationSeconds = durationSeconds,
                artistName = artistName,
                categoryName = categoryName,
                downloadDate = System.currentTimeMillis(),
            ),
        )
        Result.success()
    }

    private suspend fun fetchLyricUrl(musicId: Long): String? {
        val result = safeApiCall(json) { contentApiService.getLyrics(musicId) }
        return (result as? ApiResult.Success)?.data?.lyricUrl
    }

    private fun downloadToFile(url: String, destination: File, onProgress: (Int) -> Unit = {}): Boolean {
        return try {
            httpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) return false
                val body = response.body ?: return false
                val contentLength = body.contentLength()
                destination.parentFile?.mkdirs()
                val tempFile = File(destination.parentFile, "${destination.name}.tmp")
                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var totalRead = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            totalRead += read
                            if (contentLength > 0) onProgress(((totalRead * 100) / contentLength).toInt())
                        }
                    }
                }
                tempFile.renameTo(destination)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val KEY_MUSIC_ID = "musicId"
        const val KEY_AUDIO_URL = "audioUrl"
        const val KEY_COVER_URL = "coverUrl"
        const val KEY_TITLE = "title"
        const val KEY_ARTIST_NAME = "artistName"
        const val KEY_CATEGORY_NAME = "categoryName"
        const val KEY_RELEASE_DATE = "releaseDate"
        const val KEY_DURATION_SECONDS = "durationSeconds"
        const val KEY_PROGRESS = "progress"

        fun buildInputData(
            musicId: Long,
            audioUrl: String,
            coverUrl: String?,
            title: String,
            artistName: String?,
            categoryName: String?,
            releaseDate: String?,
            durationSeconds: Int?,
        ): Data = workDataOf(
            KEY_MUSIC_ID to musicId,
            KEY_AUDIO_URL to audioUrl,
            KEY_COVER_URL to coverUrl,
            KEY_TITLE to title,
            KEY_ARTIST_NAME to (artistName ?: "Unknown Artist"),
            KEY_CATEGORY_NAME to (categoryName ?: "Unknown"),
            KEY_RELEASE_DATE to releaseDate,
            KEY_DURATION_SECONDS to (durationSeconds ?: -1),
        )
    }
}
