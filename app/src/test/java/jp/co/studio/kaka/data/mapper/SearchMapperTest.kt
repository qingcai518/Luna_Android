package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.ArtistDto
import jp.co.studio.kaka.data.remote.dto.CategoryDto
import jp.co.studio.kaka.data.remote.dto.MusicDto
import jp.co.studio.kaka.data.remote.dto.RegionDto
import jp.co.studio.kaka.data.remote.dto.SearchResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchMapperTest {

    @Test
    fun `region maps all four locale name fields`() {
        val dto = RegionDto(regionCode = "CN", regionNameZh = "中国", regionNameJa = "中国", regionNameEn = "China")

        val domain = dto.toDomain()

        assertEquals("CN", domain.regionCode)
        assertEquals("中国", domain.regionNameZh)
        assertEquals("中国", domain.regionNameJa)
        assertEquals("China", domain.regionNameEn)
    }

    @Test
    fun `region displayName prefers zh, then en, then ja, then code`() {
        assertEquals("中国", RegionDto(regionCode = "CN", regionNameZh = "中国", regionNameEn = "China").toDomain().displayName)
        assertEquals("China", RegionDto(regionCode = "CN", regionNameEn = "China").toDomain().displayName)
        assertEquals("チャイナ", RegionDto(regionCode = "CN", regionNameJa = "チャイナ").toDomain().displayName)
        assertEquals("CN", RegionDto(regionCode = "CN").toDomain().displayName)
    }

    @Test
    fun `only the field matching the requested type is populated, others stay null not empty`() {
        // Mirrors SearchServiceImpl: a type=music search only sets `musics`, the rest are absent
        // from the JSON entirely (not empty arrays) - the mapper must preserve that distinction.
        val dto = SearchResponseDto(
            musics = listOf(MusicDto(id = 1L, title = "一路生花", audioUrl = "https://resource.qingcai518.com/music/1.mp3")),
            artists = null,
            categories = null,
            regions = null,
        )

        val domain = dto.toDomain()

        assertEquals(1, domain.musics?.size)
        assertNull(domain.artists)
        assertNull(domain.categories)
        assertNull(domain.regions)
    }

    @Test
    fun `maps every group when all are present`() {
        val dto = SearchResponseDto(
            musics = listOf(MusicDto(id = 1L, title = "一路生花", audioUrl = "https://resource.qingcai518.com/music/1.mp3")),
            artists = listOf(ArtistDto(id = 1L, name = "温奕心")),
            categories = listOf(CategoryDto(id = 1L, name = "流行")),
            regions = listOf(RegionDto(regionCode = "CN", regionNameZh = "中国")),
        )

        val domain = dto.toDomain()

        assertEquals(1, domain.musics?.size)
        assertEquals(1, domain.artists?.size)
        assertEquals(1, domain.categories?.size)
        assertEquals(1, domain.regions?.size)
        assertEquals("温奕心", domain.artists?.first()?.name)
    }
}
