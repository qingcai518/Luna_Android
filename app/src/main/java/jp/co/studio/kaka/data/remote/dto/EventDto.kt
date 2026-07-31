package jp.co.studio.kaka.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
enum class EventTypeDto {
    SEARCH, DOWNLOAD, PLAY, SKIP, LIKE, SHARE
}

/** Mirrors LunaAPI's UserEventDTO.java field-for-field. */
@Serializable
data class UserEventDto(
    val eventType: EventTypeDto,
    val musicId: Long? = null,
    val artistId: Long? = null,
    val categoryId: Long? = null,
    val keyword: String? = null,
    val playDuration: Int? = null,
    val totalDuration: Int? = null,
    val source: String? = null,
)
