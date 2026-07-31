package jp.co.studio.kaka.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LyricsDto(
    val musicId: Long,
    val lyricUrl: String? = null,
)
