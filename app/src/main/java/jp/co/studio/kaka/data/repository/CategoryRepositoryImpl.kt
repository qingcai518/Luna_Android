package jp.co.studio.kaka.data.repository

import jp.co.studio.kaka.data.mapper.toDomain
import jp.co.studio.kaka.data.remote.api.ContentApiService
import jp.co.studio.kaka.domain.model.Category
import jp.co.studio.kaka.domain.repository.CategoryRepository
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val apiService: ContentApiService,
    private val json: Json,
) : CategoryRepository {

    override suspend fun getCategories(): ApiResult<List<Category>> {
        return when (val result = safeApiCall(json) { apiService.getCategories() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.categories.map { it.toDomain() })
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}
