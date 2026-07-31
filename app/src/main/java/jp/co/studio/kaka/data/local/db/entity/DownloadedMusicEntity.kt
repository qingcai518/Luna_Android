package jp.co.studio.kaka.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_music")
data class DownloadedMusicEntity(
    @PrimaryKey val id: Long,
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
