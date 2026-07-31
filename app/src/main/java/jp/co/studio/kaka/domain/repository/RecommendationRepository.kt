package jp.co.studio.kaka.domain.repository

import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.util.ApiResult

interface RecommendationRepository {
    suspend fun getRecommendations(scene: String, limit: Int): ApiResult<List<Recommendation>>
}
