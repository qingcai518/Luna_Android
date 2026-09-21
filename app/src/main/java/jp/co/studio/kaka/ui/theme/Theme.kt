package jp.co.studio.kaka.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

private val NightColorScheme = darkColorScheme(
    primary = NightAccent,
    onPrimary = NightBackground,
    primaryContainer = NightAccent.copy(alpha = 0.16f).compositeOver(NightSurface),
    onPrimaryContainer = NightAccent,
    secondary = NightAccent,
    onSecondary = NightBackground,
    background = NightBackground,
    onBackground = NightText,
    surface = NightBackground,
    onSurface = NightText,
    surfaceVariant = NightSurface,
    onSurfaceVariant = NightText.copy(alpha = 0.66f),
    surfaceContainerLowest = NightBackground,
    surfaceContainerLow = NightBackground,
    surfaceContainer = NightSurface,
    surfaceContainerHigh = NightSurface,
    surfaceContainerHighest = NightSurfaceHigh,
    outline = NightBorder,
    outlineVariant = NightBorder,
    error = LunaRed,
)

private val MoonColorScheme = lightColorScheme(
    primary = MoonAccent,
    onPrimary = Color.White,
    primaryContainer = MoonAccent.copy(alpha = 0.12f).compositeOver(MoonSurface),
    onPrimaryContainer = MoonAccent,
    secondary = MoonAccent,
    onSecondary = Color.White,
    background = MoonBackground,
    onBackground = MoonText,
    surface = MoonBackground,
    onSurface = MoonText,
    surfaceVariant = MoonSurface,
    onSurfaceVariant = MoonText.copy(alpha = 0.66f),
    surfaceContainerLowest = MoonBackground,
    surfaceContainerLow = MoonBackground,
    surfaceContainer = MoonSurface,
    surfaceContainerHigh = MoonSurface,
    surfaceContainerHighest = MoonSurfaceHigh,
    outline = MoonBorder,
    outlineVariant = MoonBorder,
    error = LunaRed,
)

@Composable
fun LunaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 默认关闭动态取色：开着的话 Android 12+ 会用壁纸色覆盖品牌色，和 iOS 的靛蓝 + 金色对不上。
    // 想恢复 Material You 的话传 true。
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> NightColorScheme
        else -> MoonColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/** 当前生效的配色是深色还是浅色（按背景亮度判断，动态取色时也准确）。 */
val androidx.compose.material3.ColorScheme.isDark: Boolean get() = background.luminance() < 0.5f
