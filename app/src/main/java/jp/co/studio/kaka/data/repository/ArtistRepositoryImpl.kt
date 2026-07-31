package jp.co.studio.kaka.data.repository

import jp.co.studio.kaka.data.mapper.toDomain
import jp.co.studio.kaka.data.remote.api.ContentApiService
import jp.co.studio.kaka.domain.model.Artist
import jp.co.studio.kaka.domain.repository.ArtistRepository
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ArtistRepositoryImpl @Inject constructor(
    private val apiService: ContentApiService,
    private val json: Json,
) : ArtistRepository {

    override suspend fun getArtists(): ApiResult<List<Artist>> {
        return when (val result = safeApiCall(json) { apiService.getArtists() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.artists.map { it.toDomain() })
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}
