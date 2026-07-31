package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.RecommendationDto
import jp.co.studio.kaka.domain.model.Recommendation

fun RecommendationDto.toDomain(): Recommendation = Recommendation(
    music = music.toDomain(),
    score = score,
    reason = reason,
)
