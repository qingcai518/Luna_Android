package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.ArtistDto
import jp.co.studio.kaka.domain.model.Artist

fun ArtistDto.toDomain(): Artist = Artist(
    id = id,
    name = name,
    regionCode = regionCode,
    bio = bio,
    avatarUrl = avatarUrl,
)
