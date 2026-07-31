package jp.co.studio.kaka.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Long,
    val name: String,
    val coverUrl: String? = null,
    val description: String? = null,
)

@Serializable
data class CategoryListDto(
    val categories: List<CategoryDto> = emptyList(),
)
