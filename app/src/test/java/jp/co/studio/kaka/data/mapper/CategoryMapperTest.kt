package jp.co.studio.kaka.data.mapper

import jp.co.studio.kaka.data.remote.dto.CategoryDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryMapperTest {

    @Test
    fun `maps all fields when present`() {
        val dto = CategoryDto(id = 1L, name = "流行", coverUrl = "https://resource.qingcai518.com/category/1.jpg", description = "流行音乐")

        val domain = dto.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("流行", domain.name)
        assertEquals("https://resource.qingcai518.com/category/1.jpg", domain.coverUrl)
        assertEquals("流行音乐", domain.description)
    }

    @Test
    fun `preserves nulls instead of defaulting them`() {
        val dto = CategoryDto(id = 2L, name = "古风", coverUrl = null, description = null)

        val domain = dto.toDomain()

        assertNull(domain.coverUrl)
        assertNull(domain.description)
    }
}
