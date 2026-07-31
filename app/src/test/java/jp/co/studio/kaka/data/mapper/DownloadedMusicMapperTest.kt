package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.local.db.entity.DownloadedMusicEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DownloadedMusicMapperTest {

    @Test
    fun `maps every persisted field`() {
        val entity = DownloadedMusicEntity(
            id = 10L,
            title = "如愿",
            localCoverPath = "/data/user/0/jp.co.studio.kaka/files/covers/10.jpg",
            localAudioPath = "/data/user/0/jp.co.studio.kaka/files/music/10.mp3",
            localLyricsPath = "/data/user/0/jp.co.studio.kaka/files/lyrics/10.lrc",
            releaseDate = "2021-09-24",
            durationSeconds = 265,
            artistName = "王菲",
            categoryName = "流行",
            downloadDate = 1_754_000_000_000L,
        )

        val domain = entity.toDomain()

        assertEquals(10L, domain.id)
        assertEquals("如愿", domain.title)
        assertEquals("/data/user/0/jp.co.studio.kaka/files/covers/10.jpg", domain.localCoverPath)
        assertEquals("/data/user/0/jp.co.studio.kaka/files/music/10.mp3", domain.localAudioPath)
        assertEquals("/data/user/0/jp.co.studio.kaka/files/lyrics/10.lrc", domain.localLyricsPath)
        assertEquals("2021-09-24", domain.releaseDate)
        assertEquals(265, domain.durationSeconds)
        assertEquals("王菲", domain.artistName)
        assertEquals("流行", domain.categoryName)
        assertEquals(1_754_000_000_000L, domain.downloadDate)
    }

    @Test
    fun `cover and lyrics paths can be null - cover and lyrics downloads are best-effort`() {
        val entity = DownloadedMusicEntity(
            id = 11L,
            title = "Unknown Artist track",
            localCoverPath = null,
            localAudioPath = "/data/user/0/jp.co.studio.kaka/files/music/11.mp3",
            localLyricsPath = null,
            releaseDate = null,
            durationSeconds = null,
            artistName = "Unknown Artist",
            categoryName = "Unknown",
            downloadDate = 0L,
        )

        val domain = entity.toDomain()

        assertNull(domain.localCoverPath)
        assertNull(domain.localLyricsPath)
    }
}
