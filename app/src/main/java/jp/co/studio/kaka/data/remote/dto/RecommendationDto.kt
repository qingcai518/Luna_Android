package jp.co.studio.kaka.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `reason`/`score` are real, always-populated fields backed by a genuine DeepSeek call (or a
 * cold-start fallback for new users) - not legacy/unused fields. Surface `reason` in the UI.
 */
@Serializable
data class RecommendationDto(
    val music: MusicDto,
    val score: Double? = null,
    val reason: String? = null,
)
