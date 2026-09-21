package jp.co.studio.kaka.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import jp.co.studio.kaka.domain.model.AppThemeId

/**
 * 一套主题的原始颜色，数值和 iOS 端 `AppTheme` 一一对应（ThemeManager.swift），方便两端保持一致。
 *
 * 全 App 统一「强调色做底 + 页面底色做字」（按钮、播放键），强调色同时也当文字色用（当前播放的标题），
 * 所以每个主题都必须满足 **accent 与 background 的对比度 ≥ 4.5:1**（`PaletteContrastTest` 守着这条）。
 */
@Immutable
class LunaPalette(
    val id: AppThemeId,
    val background: Color,
    /** 卡片、菜单、输入框的底色（iOS 的 menuBackground）。 */
    val surface: Color,
    val border: Color,
    val accent: Color,
    val text: Color,
    /** 首页主角卡：始终是深色渐变，白色文字；强调色取自主题，必须是亮色（图标固定用深色 [HeroInk]）。 */
    val heroTop: Color,
    val heroBottom: Color,
    val heroAccent: Color,
    /** 比 [surface] 再高一层的底色（对话框、底部弹层里的块）。 */
    val surfaceHigh: Color = lerp(surface, text, 0.08f),
) {
    val isDark: Boolean get() = id.isDark
    val hero: HeroColors get() = HeroColors(heroTop, heroBottom, heroAccent)
}

@Immutable
class HeroColors(val top: Color, val bottom: Color, val accent: Color)

/** 首页主角卡的颜色，由 [LunaTheme] 按当前主题提供。 */
val LocalHeroColors = staticCompositionLocalOf { paletteOf(AppThemeId.NIGHT).hero }

fun paletteOf(id: AppThemeId): LunaPalette = when (id) {
    // 夜空：深靛蓝 + 月亮金（默认深色）
    AppThemeId.NIGHT -> LunaPalette(
        id = id,
        background = Color(0xFF0F0C2A), surface = Color(0xFF18153A), border = Color(0xFF2E2A52),
        accent = Color(0xFFFFD040), text = Color(0xFFF3F1FF),
        heroTop = Color(0xFF3B2A7A), heroBottom = Color(0xFF171243), heroAccent = Color(0xFFFFD040),
        surfaceHigh = Color(0xFF221E4A),
    )
    // 极光：深海青 + 青绿
    AppThemeId.AURORA -> LunaPalette(
        id = id,
        background = Color(0xFF061820), surface = Color(0xFF0C2530), border = Color(0xFF174255),
        accent = Color(0xFF4DD9C0), text = Color(0xFFE6F7F4),
        heroTop = Color(0xFF0F6070), heroBottom = Color(0xFF082A3A), heroAccent = Color(0xFF4DD9C0),
    )
    // 落日：深梅紫 + 暖珊瑚
    AppThemeId.SUNSET -> LunaPalette(
        id = id,
        background = Color(0xFF1A0A20), surface = Color(0xFF28102E), border = Color(0xFF4A1E58),
        accent = Color(0xFFFF8C7A), text = Color(0xFFFBEFF3),
        heroTop = Color(0xFF8A2F6B), heroBottom = Color(0xFF2A0F38), heroAccent = Color(0xFFFF8C7A),
    )
    // 曜石：纯黑（OLED 省电）+ 信号橙，高对比
    AppThemeId.ONYX -> LunaPalette(
        id = id,
        background = Color(0xFF000000), surface = Color(0xFF111114), border = Color(0xFF2A2A30),
        accent = Color(0xFFFF7A45), text = Color(0xFFFFFFFF),
        heroTop = Color(0xFF34343C), heroBottom = Color(0xFF0C0C0F), heroAccent = Color(0xFFFF7A45),
    )
    // 月白：白 + 琥珀金（默认浅色）
    AppThemeId.MOONLIGHT -> LunaPalette(
        id = id,
        background = Color(0xFFFFFFFF), surface = Color(0xFFF0EEFF), border = Color(0xFFC8C2E8),
        accent = Color(0xFF8F6200), text = Color(0xFF1B1740),
        heroTop = Color(0xFF4B3A9A), heroBottom = Color(0xFF221A5E), heroAccent = Color(0xFFFFD040),
        surfaceHigh = Color(0xFFE6E2FA),
    )
    // 纸白：暖纸白 + 墨蓝，高对比
    AppThemeId.PAPER -> LunaPalette(
        id = id,
        background = Color(0xFFFAFAF7), surface = Color(0xFFF0EFE9), border = Color(0xFFD3D0C4),
        accent = Color(0xFF1E4BD2), text = Color(0xFF14161C),
        heroTop = Color(0xFF243A8C), heroBottom = Color(0xFF0B1236), heroAccent = Color(0xFF8FB0FF),
    )
    // 樱花：浅粉 + 深玫红
    AppThemeId.SAKURA -> LunaPalette(
        id = id,
        background = Color(0xFFFFF7F9), surface = Color(0xFFFDE9EE), border = Color(0xFFF0BFCB),
        accent = Color(0xFFB0245A), text = Color(0xFF3B1327),
        heroTop = Color(0xFF9B2C5E), heroBottom = Color(0xFF34102E), heroAccent = Color(0xFFFF9DBB),
    )
    // 薄荷：浅薄荷 + 森林绿
    AppThemeId.MINT -> LunaPalette(
        id = id,
        background = Color(0xFFF3FBF7), surface = Color(0xFFE1F4EA), border = Color(0xFFB4DCC8),
        accent = Color(0xFF0A7550), text = Color(0xFF0D2A20),
        heroTop = Color(0xFF0F6B57), heroBottom = Color(0xFF06251F), heroAccent = Color(0xFF7BE0B4),
    )
}

fun LunaPalette.toColorScheme(): ColorScheme {
    // 深色主题上强调色色块的浅底更深一点（0.16），浅色主题上更淡（0.12），和之前的夜空 / 月白一致
    val container = accent.copy(alpha = if (isDark) 0.16f else 0.12f).compositeOver(surface)
    return if (isDark) {
        darkColorScheme(
            primary = accent, onPrimary = background,
            primaryContainer = container, onPrimaryContainer = accent,
            secondary = accent, onSecondary = background,
            background = background, onBackground = text,
            surface = background, onSurface = text,
            surfaceVariant = surface, onSurfaceVariant = text.copy(alpha = 0.66f),
            surfaceContainerLowest = background, surfaceContainerLow = background,
            surfaceContainer = surface, surfaceContainerHigh = surface, surfaceContainerHighest = surfaceHigh,
            outline = border, outlineVariant = border,
            error = LunaRed,
        )
    } else {
        lightColorScheme(
            primary = accent, onPrimary = background,
            primaryContainer = container, onPrimaryContainer = accent,
            secondary = accent, onSecondary = background,
            background = background, onBackground = text,
            surface = background, onSurface = text,
            surfaceVariant = surface, onSurfaceVariant = text.copy(alpha = 0.66f),
            surfaceContainerLowest = background, surfaceContainerLow = background,
            surfaceContainer = surface, surfaceContainerHigh = surface, surfaceContainerHighest = surfaceHigh,
            outline = border, outlineVariant = border,
            error = LunaRed,
        )
    }
}
