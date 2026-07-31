package jp.co.studio.kaka.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArtistDto(
    val id: Long,
    val name: String,
    // Only populated when fetched via the standalone GET /artist endpoint - MusicVO.artist.regionCode
    // is always null because that SQL join never selects it.
    val regionCode: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class ArtistListDto(
    val artists: List<ArtistDto> = emptyList(),
)
