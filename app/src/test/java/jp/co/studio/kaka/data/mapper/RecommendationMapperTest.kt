package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.MusicDto
import jp.co.studio.kaka.data.remote.dto.RecommendationDto
import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendationMapperTest {

    private val musicDto = MusicDto(id = 1L, title = "鱿鱼玻璃", audioUrl = "https://resource.qingcai518.com/music/1.mp3")

    @Test
    fun `maps music, score, and reason - both are real fields to surface in the UI`() {
        val dto = RecommendationDto(music = musicDto, score = 0.92, reason = "3D电子音乐首选，完美匹配偏好")

        val domain = dto.toDomain()

        assertEquals(1L, domain.music.id)
        assertEquals(0.92, domain.score)
        assertEquals("3D电子音乐首选，完美匹配偏好", domain.reason)
    }

    @Test
    fun `maps cold-start fallback shape with default score and reason`() {
        val dto = RecommendationDto(music = musicDto, score = 0.5, reason = "热门推荐")

        val domain = dto.toDomain()

        assertEquals(0.5, domain.score)
        assertEquals("热门推荐", domain.reason)
    }
}
