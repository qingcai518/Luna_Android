package jp.co.studio.kaka.domain.repository

import jp.co.studio.kaka.domain.model.LyricLine
import jp.co.studio.kaka.util.ApiResult

interface LyricsRepository {
    suspend fun getLyrics(musicId: Long): ApiResult<List<LyricLine>>
}
