package jp.co.studio.kaka.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Luna 品牌色板：与 iOS 端的四套主题里默认的「夜空」（深色）和「月白」（浅色）一致。
 * 之前是 Material 模板的紫色兜底 + Android 12+ 的动态取色（跟着壁纸变），和 iOS 的靛蓝 + 金色对不上。
 */

// 夜空（深色）：深靛蓝 + 月亮金
val NightBackground = Color(0xFF0F0C2A)
val NightSurface = Color(0xFF18153A)
val NightSurfaceHigh = Color(0xFF221E4A)
val NightBorder = Color(0xFF2E2A52)
val NightAccent = Color(0xFFFFD040)
val NightText = Color(0xFFF3F1FF)

// 月白（浅色）：白 + 琥珀金（#8F6200 在白底上对比度 ≥ 4.5:1）
val MoonBackground = Color(0xFFFFFFFF)
val MoonSurface = Color(0xFFF0EEFF)
val MoonSurfaceHigh = Color(0xFFE6E2FA)
val MoonBorder = Color(0xFFC8C2E8)
val MoonAccent = Color(0xFF8F6200)
val MoonText = Color(0xFF1B1740)

// 首页主角卡：始终是深色渐变（浅色主题下也是），强调色固定用金色
val HeroTopDark = Color(0xFF3B2A7A)
val HeroBottomDark = Color(0xFF171243)
val HeroTopLight = Color(0xFF4B3A9A)
val HeroBottomLight = Color(0xFF221A5E)
val HeroAccent = Color(0xFFFFD040)
val HeroInk = Color(0xFF0F0C2A)

// 破坏性操作
val LunaRed = Color(0xFFD15E5E)
