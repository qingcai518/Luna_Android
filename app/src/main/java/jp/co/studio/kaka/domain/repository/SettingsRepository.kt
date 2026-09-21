package jp.co.studio.kaka.domain.repository

import jp.co.studio.kaka.domain.model.AppThemeId
import jp.co.studio.kaka.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>

    /** 用户选的主题；实际生效的主题还要结合 [themeMode] 和系统明暗，见 `resolveAppTheme`。 */
    val themeId: Flow<AppThemeId>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setThemeId(id: AppThemeId)
}
