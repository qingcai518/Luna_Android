package jp.co.studio.kaka.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import jp.co.studio.kaka.domain.model.AppThemeId

@Composable
fun LunaTheme(
    // 实际生效的主题：由 MainActivity 用 resolveAppTheme(模式, 所选主题, 系统明暗) 算出来
    theme: AppThemeId = AppThemeId.NIGHT,
    // 默认关闭动态取色：开着的话 Android 12+ 会用壁纸色覆盖品牌色，和 iOS 的主题配色对不上。
    // 想恢复 Material You 的话传 true（此时只按主题的明暗选 Material You 的深 / 浅色）。
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = paletteOf(theme)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (palette.isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> palette.toColorScheme()
    }

    CompositionLocalProvider(LocalHeroColors provides palette.hero) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/** 当前生效的配色是深色还是浅色（按背景亮度判断，动态取色时也准确）。 */
val androidx.compose.material3.ColorScheme.isDark: Boolean get() = background.luminance() < 0.5f
