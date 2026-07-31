package jp.co.studio.kaka.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MusicDto(
    val id: Long,
    val title: String,
    // Signed OSS/CDN URL, expires ~1 hour after the response was generated - never cache/reuse
    // this across app sessions, only within the lifetime of the screen that fetched it.
    val coverUrl: String? = null,
    val audioUrl: String,
    val releaseDate: String? = null,
    val durationSeconds: Int? = null,
    val artist: ArtistDto? = null,
    val category: CategoryDto? = null,
)

@Serializable
data class MusicListDto(
    val musics: List<MusicDto> = emptyList(),
)
