package jp.co.studio.kaka.data.repository

import jp.co.studio.kaka.data.local.db.dao.DownloadedMusicDao
import jp.co.studio.kaka.data.remote.api.ContentApiService
import jp.co.studio.kaka.di.DownloadClient
import jp.co.studio.kaka.domain.model.LyricLine
import jp.co.studio.kaka.domain.repository.LyricsRepository
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.LrcParser
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

/** Prefers the locally downloaded .lrc file (if any) over a fresh network fetch. */
class LyricsRepositoryImpl @Inject constructor(
    private val contentApiService: ContentApiService,
    private val json: Json,
    @DownloadClient private val httpClient: OkHttpClient,
    private val downloadedMusicDao: DownloadedMusicDao,
) : LyricsRepository {

    override suspend fun getLyrics(musicId: Long): ApiResult<List<LyricLine>> = withContext(Dispatchers.IO) {
        val localPath = downloadedMusicDao.getById(musicId)?.localLyricsPath
        if (localPath != null) {
            val localFile = File(localPath)
            if (localFile.exists()) {
                return@withContext ApiResult.Success(LrcParser.parse(localFile.readText()))
            }
        }

        when (val result = safeApiCall(json) { contentApiService.getLyrics(musicId) }) {
            is ApiResult.Success -> {
                val lyricUrl = result.data.lyricUrl
                    ?: return@withContext ApiResult.Success(emptyList())
                val text = runCatching {
                    httpClient.newCall(Request.Builder().url(lyricUrl).build()).execute().use { response ->
                        if (!response.isSuccessful) null else response.body?.string()
                    }
                }.getOrNull()
                ApiResult.Success(text?.let { LrcParser.parse(it) } ?: emptyList())
            }
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}
