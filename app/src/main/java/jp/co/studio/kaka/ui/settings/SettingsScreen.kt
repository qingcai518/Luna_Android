package jp.co.studio.kaka.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    var languageTag by remember { mutableStateOf(currentAppLanguageTag()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SectionHeader(stringResource(R.string.settings_appearance))
            SelectableOption(
                label = stringResource(R.string.common_follow_system),
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
            )
            SelectableOption(
                label = stringResource(R.string.theme_light),
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
            )
            SelectableOption(
                label = stringResource(R.string.theme_dark),
                selected = themeMode == ThemeMode.DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
            )

            HorizontalDivider()

            SectionHeader(stringResource(R.string.settings_language))
            SelectableOption(
                label = stringResource(R.string.common_follow_system),
                selected = languageTag == null,
                onClick = {
                    setAppLanguageTag(null)
                    languageTag = null
                },
            )
            SelectableOption(
                label = "中文",
                selected = languageTag == "zh",
                onClick = {
                    setAppLanguageTag("zh")
                    languageTag = "zh"
                },
            )
            SelectableOption(
                label = "English",
                selected = languageTag == "en",
                onClick = {
                    setAppLanguageTag("en")
                    languageTag = "en"
                },
            )
            SelectableOption(
                label = "日本語",
                selected = languageTag == "ja",
                onClick = {
                    setAppLanguageTag("ja")
                    languageTag = "ja"
                },
            )
        }
    }
}

/** Reads the current per-app language preference (null = following the system locale). */
private fun currentAppLanguageTag(): String? {
    val locales = AppCompatDelegate.getApplicationLocales()
    return if (locales.isEmpty) null else locales[0]?.language
}

/**
 * Persists via AppCompat's per-app language storage (SharedPreferences pre-API33, platform
 * LocaleManager on API33+) and triggers an automatic activity recreation - no app-owned
 * DataStore entry needed, unlike theme.
 */
private fun setAppLanguageTag(languageTag: String?) {
    val locales = if (languageTag == null) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(languageTag)
    }
    AppCompatDelegate.setApplicationLocales(locales)
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SelectableOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
