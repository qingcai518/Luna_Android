package jp.co.studio.kaka.ui.theme

import androidx.compose.ui.graphics.Color
import jp.co.studio.kaka.domain.model.AppThemeId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * 守住配色约定：全 App 是「强调色做底 + 页面底色做字」，强调色也当文字色用，
 * 所以每个主题的 accent 与 background / surface 的对比度都要 ≥ 4.5:1（WCAG AA）。
 */
class PaletteContrastTest {

    private fun channel(c: Float): Double = if (c <= 0.03928f) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    private fun luminance(color: Color): Double =
        0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    @Test
    fun `every theme has a palette whose id matches`() {
        AppThemeId.entries.forEach { assertEquals(it, paletteOf(it).id) }
    }

    @Test
    fun `dark themes have dark backgrounds and light themes have light backgrounds`() {
        AppThemeId.entries.forEach { id ->
            val bg = luminance(paletteOf(id).background)
            if (id.isDark) assertTrue("$id background should be dark", bg < 0.2) else assertTrue("$id background should be light", bg > 0.7)
        }
    }

    @Test
    fun `accent is readable on the page background and on cards`() {
        AppThemeId.entries.forEach { id ->
            val p = paletteOf(id)
            assertTrue("$id accent/background = ${contrast(p.accent, p.background)}", contrast(p.accent, p.background) >= 4.5)
            assertTrue("$id accent/surface = ${contrast(p.accent, p.surface)}", contrast(p.accent, p.surface) >= 4.5)
        }
    }

    @Test
    fun `body text is readable on background and surface`() {
        AppThemeId.entries.forEach { id ->
            val p = paletteOf(id)
            assertTrue("$id text/background", contrast(p.text, p.background) >= 7.0)
            assertTrue("$id text/surface", contrast(p.text, p.surface) >= 7.0)
        }
    }

    @Test
    fun `hero card keeps white text readable and its accent button icon readable`() {
        AppThemeId.entries.forEach { id ->
            val p = paletteOf(id)
            assertTrue("$id white/heroTop", contrast(Color.White, p.heroTop) >= 4.5)
            assertTrue("$id white/heroBottom", contrast(Color.White, p.heroBottom) >= 4.5)
            assertTrue("$id heroAccent/HeroInk", contrast(p.heroAccent, HeroInk) >= 4.5)
        }
    }
}
