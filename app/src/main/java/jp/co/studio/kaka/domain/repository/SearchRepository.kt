package jp.co.studio.kaka.domain.repository

import jp.co.studio.kaka.domain.model.SearchResult
import jp.co.studio.kaka.util.ApiResult
import jp.co.studio.kaka.util.SearchType

interface SearchRepository {
    suspend fun search(keyword: String, type: SearchType, page: Int, size: Int): ApiResult<SearchResult>
}
