package jp.co.studio.kaka.domain.model

/**
 * 应用主题（对应 iOS 的 `AppTheme`）。声明顺序就是设置页的显示顺序：
 * 第一行 4 个深色主题，第二行 4 个浅色主题。具体配色见 `ui/theme/Palette.kt`。
 */
enum class AppThemeId(val isDark: Boolean) {
    NIGHT(true),
    AURORA(true),
    SUNSET(true),
    ONYX(true),
    MOONLIGHT(false),
    PAPER(false),
    SAKURA(false),
    MINT(false),
}

/**
 * 实际生效的主题（和 iOS 的 `ThemeManager.effectiveTheme` 同一套规则）。
 *
 * [mode] 决定这一刻用深色还是浅色（跟随系统时看 [systemDark]）；所选主题 [selected] 的明暗和它一致就用它，
 * 不一致就退回默认的「夜空」（深色）/「月白」（浅色）。
 */
fun resolveAppTheme(mode: ThemeMode, selected: AppThemeId, systemDark: Boolean): AppThemeId {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    return when {
        selected.isDark == dark -> selected
        dark -> AppThemeId.NIGHT
        else -> AppThemeId.MOONLIGHT
    }
}
