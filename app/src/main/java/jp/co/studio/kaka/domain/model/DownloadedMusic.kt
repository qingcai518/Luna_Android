package jp.co.studio.kaka.domain.model

data class DownloadedMusic(
    val id: Long,
    val title: String,
    val localCoverPath: String?,
    val localAudioPath: String,
    val localLyricsPath: String?,
    val releaseDate: String?,
    val durationSeconds: Int?,
    val artistName: String,
    val categoryName: String,
    val downloadDate: Long,
)

/** Converts a downloaded record back into a playable [Music] - artist/category are placeholders since only names are persisted locally. */
fun DownloadedMusic.toMusic(): Music = Music(
    id = id,
    title = title,
    coverUrl = localCoverPath,
    audioUrl = "file://$localAudioPath",
    releaseDate = releaseDate,
    durationSeconds = durationSeconds,
    artist = Artist(id = -1, name = artistName, regionCode = null, bio = null, avatarUrl = null),
    category = Category(id = -1, name = categoryName, coverUrl = null, description = null),
)
