package jp.co.studio.kaka.data.repository

import jp.co.studio.kaka.data.local.datastore.UserPreferencesDataStore
import jp.co.studio.kaka.domain.model.AppThemeId
import jp.co.studio.kaka.domain.model.ThemeMode
import jp.co.studio.kaka.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode> = userPreferencesDataStore.themeMode

    override val themeId: Flow<AppThemeId> = userPreferencesDataStore.themeId

    override suspend fun setThemeMode(mode: ThemeMode) {
        userPreferencesDataStore.setThemeMode(mode)
    }

    override suspend fun setThemeId(id: AppThemeId) {
        userPreferencesDataStore.setThemeId(id)
    }
}
