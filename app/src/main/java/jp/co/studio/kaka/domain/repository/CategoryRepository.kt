package jp.co.studio.kaka.domain.repository

import jp.co.studio.kaka.domain.model.Category
import jp.co.studio.kaka.util.ApiResult

interface CategoryRepository {
    suspend fun getCategories(): ApiResult<List<Category>>
}
