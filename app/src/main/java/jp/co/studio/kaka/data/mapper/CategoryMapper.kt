package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.CategoryDto
import jp.co.studio.kaka.domain.model.Category

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    coverUrl = coverUrl,
    description = description,
)
