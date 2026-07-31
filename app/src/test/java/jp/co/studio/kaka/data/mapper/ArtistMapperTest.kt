package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.ArtistDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArtistMapperTest {

    @Test
    fun `maps all fields when present`() {
        val dto = ArtistDto(
            id = 1L,
            name = "周杰伦",
            regionCode = "CN_SUB",
            bio = "台湾著名歌手",
            avatarUrl = "https://resource.qingcai518.com/artist/1.jpg",
        )

        val domain = dto.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("周杰伦", domain.name)
        assertEquals("CN_SUB", domain.regionCode)
        assertEquals("台湾著名歌手", domain.bio)
        assertEquals("https://resource.qingcai518.com/artist/1.jpg", domain.avatarUrl)
    }

    @Test
    fun `preserves nulls instead of defaulting them`() {
        // MusicVO.artist.regionCode is always null unless fetched via the standalone /artist
        // endpoint - the mapper must pass that through as-is, not coerce it to a placeholder.
        val dto = ArtistDto(id = 2L, name = "温奕心", regionCode = null, bio = null, avatarUrl = null)

        val domain = dto.toDomain()

        assertNull(domain.regionCode)
        assertNull(domain.bio)
        assertNull(domain.avatarUrl)
    }
}
