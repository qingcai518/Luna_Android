package jp.co.studio.kaka.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.model.AppThemeId
import jp.co.studio.kaka.domain.model.ThemeMode
import jp.co.studio.kaka.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val themeId: StateFlow<AppThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppThemeId.NIGHT)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    /**
     * 选主题。强制了浅色 / 深色时，把模式改成和这个主题一致，保证点了就能看到变化；
     * 跟随系统时保持不变（系统是浅色就用所选的浅色主题，深色同理）。
     * 先写主题、再写模式：反过来的话会在中间闪一下默认主题。
     */
    fun selectTheme(id: AppThemeId) {
        viewModelScope.launch {
            settingsRepository.setThemeId(id)
            // 直接问仓库，不读 themeMode.value：它是 WhileSubscribed 的，没人订阅时永远是初始值
            if (settingsRepository.themeMode.first() != ThemeMode.SYSTEM) {
                settingsRepository.setThemeMode(if (id.isDark) ThemeMode.DARK else ThemeMode.LIGHT)
            }
        }
    }
}
