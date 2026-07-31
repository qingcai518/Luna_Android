package jp.co.studio.kaka.domain.model

/**
 * `reason`/`score` are real, DeepSeek-backed fields (or a cold-start fallback for new users) -
 * surface `reason` in the UI, unlike iOS which fetches but never displays it.
 */
data class Recommendation(
    val music: Music,
    val score: Double?,
    val reason: String?,
)
