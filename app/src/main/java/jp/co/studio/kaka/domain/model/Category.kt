package jp.co.studio.kaka.domain.model

data class Category(
    val id: Long,
    val name: String,
    val coverUrl: String?,
    val description: String?,
)
