package jp.co.studio.kaka.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemeIdTest {

    @Test
    fun `picker order is four dark themes then four light themes`() {
        val entries = AppThemeId.entries
        assertEquals(8, entries.size)
        assertTrue(entries.take(4).all { it.isDark })
        assertTrue(entries.drop(4).none { it.isDark })
    }

    @Test
    fun `following system uses the chosen theme when its brightness matches`() {
        assertEquals(AppThemeId.ONYX, resolveAppTheme(ThemeMode.SYSTEM, AppThemeId.ONYX, systemDark = true))
        assertEquals(AppThemeId.SAKURA, resolveAppTheme(ThemeMode.SYSTEM, AppThemeId.SAKURA, systemDark = false))
    }

    @Test
    fun `following system falls back to default theme of the system brightness`() {
        // 选了浅色主题，但系统是深色 → 夜空；选了深色主题，但系统是浅色 → 月白
        assertEquals(AppThemeId.NIGHT, resolveAppTheme(ThemeMode.SYSTEM, AppThemeId.MINT, systemDark = true))
        assertEquals(AppThemeId.MOONLIGHT, resolveAppTheme(ThemeMode.SYSTEM, AppThemeId.AURORA, systemDark = false))
    }

    @Test
    fun `forced light or dark ignores the system brightness`() {
        assertEquals(AppThemeId.PAPER, resolveAppTheme(ThemeMode.LIGHT, AppThemeId.PAPER, systemDark = true))
        assertEquals(AppThemeId.SUNSET, resolveAppTheme(ThemeMode.DARK, AppThemeId.SUNSET, systemDark = false))
        assertEquals(AppThemeId.MOONLIGHT, resolveAppTheme(ThemeMode.LIGHT, AppThemeId.NIGHT, systemDark = true))
        assertEquals(AppThemeId.NIGHT, resolveAppTheme(ThemeMode.DARK, AppThemeId.MOONLIGHT, systemDark = false))
    }

    @Test
    fun `the default night theme resolves to itself in every dark situation`() {
        assertEquals(AppThemeId.NIGHT, resolveAppTheme(ThemeMode.SYSTEM, AppThemeId.NIGHT, systemDark = true))
        assertEquals(AppThemeId.NIGHT, resolveAppTheme(ThemeMode.DARK, AppThemeId.NIGHT, systemDark = false))
    }
}
