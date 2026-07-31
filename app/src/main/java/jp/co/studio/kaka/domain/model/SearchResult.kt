package jp.co.studio.kaka.domain.model

/**
 * Only the field matching the requested [jp.co.studio.kaka.util.SearchType] is populated by the
 * backend - the rest stay null (not empty lists). Do not treat "null" here as "no results" for
 * a group that wasn't requested.
 */
data class SearchResult(
    val musics: List<Music>? = null,
    val artists: List<Artist>? = null,
    val categories: List<Category>? = null,
    val regions: List<Region>? = null,
)
