package jp.co.studio.kaka.data.repository

import jp.co.studio.kaka.data.mapper.toDomain
import jp.co.studio.kaka.data.remote.api.ContentApiService
import jp.co.studio.kaka.domain.model.SearchResult
import jp.co.studio.kaka.domain.repository.SearchRepository
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.SearchType
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val apiService: ContentApiService,
    private val json: Json,
) : SearchRepository {

    override suspend fun search(keyword: String, type: SearchType, page: Int, size: Int): ApiResult<SearchResult> {
        return when (
            val result = safeApiCall(json) { apiService.search(keyword, type.apiValue, page, size) }
        ) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}
