package jp.co.studio.kaka.domain.model

data class Artist(
    val id: Long,
    val name: String,
    val regionCode: String?,
    val bio: String?,
    val avatarUrl: String?,
)
