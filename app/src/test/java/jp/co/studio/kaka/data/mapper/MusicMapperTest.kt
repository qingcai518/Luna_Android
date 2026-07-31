package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.ArtistDto
import jp.co.studio.kaka.data.remote.dto.CategoryDto
import jp.co.studio.kaka.data.remote.dto.MusicDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class MusicMapperTest {

    @Test
    fun `maps nested artist and category when present`() {
        val dto = MusicDto(
            id = 10L,
            title = "如愿",
            coverUrl = "https://resource.qingcai518.com/cover/10.jpg",
            audioUrl = "https://resource.qingcai518.com/music/10.mp3",
            releaseDate = "2021-09-24",
            durationSeconds = 265,
            artist = ArtistDto(id = 14L, name = "王菲"),
            category = CategoryDto(id = 1L, name = "流行"),
        )

        val domain = dto.toDomain()

        assertEquals(10L, domain.id)
        assertEquals("如愿", domain.title)
        assertEquals("2021-09-24", domain.releaseDate)
        assertEquals(265, domain.durationSeconds)
        assertNotNull(domain.artist)
        assertEquals("王菲", domain.artist?.name)
        assertNotNull(domain.category)
        assertEquals("流行", domain.category?.name)
    }

    @Test
    fun `leaves artist and category null when absent from the response`() {
        // MusicVO.artist/category can be null (nullable FK on the backend row) - the mapper
        // must not synthesize placeholders here, that's DownloadedMusic.toMusic()'s job.
        val dto = MusicDto(id = 11L, title = "未知曲目", audioUrl = "https://resource.qingcai518.com/music/11.mp3")

        val domain = dto.toDomain()

        assertNull(domain.artist)
        assertNull(domain.category)
        assertNull(domain.coverUrl)
        assertNull(domain.releaseDate)
        assertNull(domain.durationSeconds)
    }
}
