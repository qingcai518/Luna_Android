package jp.co.studio.kaka

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import jp.co.studio.kaka.domain.model.resolveAppTheme
import jp.co.studio.kaka.ui.navigation.LunaRoot
import jp.co.studio.kaka.ui.settings.SettingsViewModel
import jp.co.studio.kaka.ui.theme.LunaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val themeId by settingsViewModel.themeId.collectAsStateWithLifecycle()
            val theme = resolveAppTheme(themeMode, themeId, systemDark = isSystemInDarkTheme())

            // 状态栏 / 导航栏图标的深浅要跟着「主题」走，而不是跟着系统：
            // 手动选了浅色主题、系统却是深色（或反过来）时，默认的 enableEdgeToEdge() 会让时钟和电量图标和背景同色。
            DisposableEffect(theme.isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { theme.isDark },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF),
                        darkScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B),
                    ) { theme.isDark },
                )
                onDispose {}
            }

            LunaTheme(theme = theme) {
                LunaRoot()
            }
        }
    }
}
