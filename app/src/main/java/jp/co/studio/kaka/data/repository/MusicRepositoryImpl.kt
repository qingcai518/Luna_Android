package jp.co.studio.kaka.data.repository

import jp.co.studio.kaka.data.mapper.toDomain
import jp.co.studio.kaka.data.remote.api.ContentApiService
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.repository.MusicRepository
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val apiService: ContentApiService,
    private val json: Json,
) : MusicRepository {

    override suspend fun getMusicByArtist(artistId: Long): ApiResult<List<Music>> {
        return when (val result = safeApiCall(json) { apiService.getMusicByArtist(artistId) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.musics.map { it.toDomain() })
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }

    override suspend fun getMusicByCategory(categoryId: Long): ApiResult<List<Music>> {
        return when (val result = safeApiCall(json) { apiService.getMusicByCategory(categoryId) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.musics.map { it.toDomain() })
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}
