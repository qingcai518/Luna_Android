package jp.co.studio.kaka.data.repository

import jp.co.studio.kaka.data.mapper.toDomain
import jp.co.studio.kaka.data.remote.api.ContentApiService
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.domain.repository.RecommendationRepository
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val apiService: ContentApiService,
    private val json: Json,
) : RecommendationRepository {

    override suspend fun getRecommendations(scene: String, limit: Int): ApiResult<List<Recommendation>> {
        return when (val result = safeApiCall(json) { apiService.getRecommendations(scene, limit) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}
