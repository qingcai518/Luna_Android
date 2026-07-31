package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.MusicDto
import jp.co.studio.kaka.domain.model.Music

fun MusicDto.toDomain(): Music = Music(
    id = id,
    title = title,
    coverUrl = coverUrl,
    audioUrl = audioUrl,
    releaseDate = releaseDate,
    durationSeconds = durationSeconds,
    artist = artist?.toDomain(),
    category = category?.toDomain(),
)
