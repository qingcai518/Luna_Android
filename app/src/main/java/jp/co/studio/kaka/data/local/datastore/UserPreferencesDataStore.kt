package jp.co.studio.kaka.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import jp.co.studio.kaka.domain.model.ThemeMode
import jp.co.studio.kaka.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val AVATAR_URL = stringPreferencesKey("avatar_url")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    private val safePreferences: Flow<Preferences> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val currentUser: Flow<User?> = safePreferences.map { prefs ->
        val userId = prefs[Keys.USER_ID] ?: return@map null
        val username = prefs[Keys.USERNAME] ?: return@map null
        val email = prefs[Keys.EMAIL] ?: return@map null
        User(userId, username, email, prefs[Keys.AVATAR_URL])
    }

    val themeMode: Flow<ThemeMode> = safePreferences.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { raw ->
            runCatching { ThemeMode.valueOf(raw) }.getOrNull()
        } ?: ThemeMode.SYSTEM
    }

    suspend fun saveUser(user: User) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = user.userId
            prefs[Keys.USERNAME] = user.username
            prefs[Keys.EMAIL] = user.email
            user.avatarUrl?.let { prefs[Keys.AVATAR_URL] = it }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }
    }

    suspend fun clearUser() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.EMAIL)
            prefs.remove(Keys.AVATAR_URL)
        }
    }
}
