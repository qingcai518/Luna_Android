package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.local.db.entity.DownloadedMusicEntity
import jp.co.studio.kaka.domain.model.DownloadedMusic

fun DownloadedMusicEntity.toDomain(): DownloadedMusic = DownloadedMusic(
    id = id,
    title = title,
    localCoverPath = localCoverPath,
    localAudioPath = localAudioPath,
    localLyricsPath = localLyricsPath,
    releaseDate = releaseDate,
    durationSeconds = durationSeconds,
    artistName = artistName,
    categoryName = categoryName,
    downloadDate = downloadDate,
)
