package jp.co.studio.kaka.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegionDto(
    val regionCode: String? = null,
    val regionNameZh: String? = null,
    val regionNameJa: String? = null,
    val regionNameEn: String? = null,
)

@Serializable
data class PageInfoDto(
    val page: Int? = null,
    val size: Int? = null,
    val total: Long? = null,
    val totalPages: Int? = null,
)

/**
 * Only the field(s) matching the requested `type` are populated by the backend; the rest are
 * null (not empty lists). `pageInfo` is defined in the DTO shape but is never actually set by
 * SearchServiceImpl - treat it as always null, do not build pagination UI depending on it.
 */
@Serializable
data class SearchResponseDto(
    val musics: List<MusicDto>? = null,
    val artists: List<ArtistDto>? = null,
    val categories: List<CategoryDto>? = null,
    val regions: List<RegionDto>? = null,
    val pageInfo: PageInfoDto? = null,
)
