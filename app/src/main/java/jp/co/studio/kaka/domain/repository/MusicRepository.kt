package jp.co.studio.kaka.domain.repository

import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.util.ApiResult

interface MusicRepository {
    suspend fun getMusicByArtist(artistId: Long): ApiResult<List<Music>>
    suspend fun getMusicByCategory(categoryId: Long): ApiResult<List<Music>>
}
