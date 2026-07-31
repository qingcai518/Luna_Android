package jp.co.studio.kaka.domain.model

data class Music(
    val id: Long,
    val title: String,
    val coverUrl: String?,
    val audioUrl: String,
    val releaseDate: String?,
    val durationSeconds: Int?,
    val artist: Artist?,
    val category: Category?,
)
