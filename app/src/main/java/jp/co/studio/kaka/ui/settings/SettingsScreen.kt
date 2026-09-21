package jp.co.studio.kaka.ui.settings

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.AppThemeId
import jp.co.studio.kaka.domain.model.ThemeMode
import jp.co.studio.kaka.ui.theme.paletteOf

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    var languageTag by remember { mutableStateOf(currentAppLanguageTag()) }

    SettingsContent(
        themeMode = themeMode,
        themeId = themeId,
        languageTag = languageTag,
        onBackClick = onBackClick,
        onThemeModeChange = viewModel::setThemeMode,
        onThemeSelect = viewModel::selectTheme,
        onLanguageChange = { tag ->
            setAppLanguageTag(tag)
            languageTag = tag
        },
    )
}

/** 无状态的设置页内容：所有数据和回调都从参数进来，方便预览和截图。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    themeMode: ThemeMode,
    themeId: AppThemeId,
    languageTag: String?,
    onBackClick: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onThemeSelect: (AppThemeId) -> Unit,
    onLanguageChange: (String?) -> Unit,
) {
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
        Column(modifier = Modifier.padding(innerPadding).verticalScroll(rememberScrollState())) {
            SectionHeader(stringResource(R.string.settings_appearance))
            SelectableOption(
                label = stringResource(R.string.common_follow_system),
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
            )
            SelectableOption(
                label = stringResource(R.string.theme_light),
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { onThemeModeChange(ThemeMode.LIGHT) },
            )
            SelectableOption(
                label = stringResource(R.string.theme_dark),
                selected = themeMode == ThemeMode.DARK,
                onClick = { onThemeModeChange(ThemeMode.DARK) },
            )

            HorizontalDivider()

            SectionHeader(stringResource(R.string.settings_theme))
            ThemePicker(selected = themeId, onSelect = onThemeSelect)
            if (themeMode == ThemeMode.SYSTEM) {
                Text(
                    text = stringResource(R.string.settings_theme_follow_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }

            HorizontalDivider()

            SectionHeader(stringResource(R.string.settings_language))
            SelectableOption(
                label = stringResource(R.string.common_follow_system),
                selected = languageTag == null,
                onClick = { onLanguageChange(null) },
            )
            SelectableOption(label = "中文", selected = languageTag == "zh", onClick = { onLanguageChange("zh") })
            SelectableOption(label = "English", selected = languageTag == "en", onClick = { onLanguageChange("en") })
            SelectableOption(label = "日本語", selected = languageTag == "ja", onClick = { onLanguageChange("ja") })
        }
    }
}

/** 主题选择：每行 4 个色块（第一行深色、第二行浅色，顺序见 [AppThemeId]）。 */
@Composable
private fun ThemePicker(selected: AppThemeId, onSelect: (AppThemeId) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        AppThemeId.entries.chunked(THEME_COLUMNS).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                row.forEach { id ->
                    ThemeSwatch(
                        id = id,
                        isSelected = id == selected,
                        onClick = { onSelect(id) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private const val THEME_COLUMNS = 4

/** 一个主题色块：主题底色的圆 + 中间一点主题强调色 + 下方主题名；选中时圈变成当前主题的强调色、加粗。 */
@Composable
private fun ThemeSwatch(id: AppThemeId, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = paletteOf(id)
    val label = stringResource(id.labelRes())
    Column(
        modifier = modifier
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(palette.background)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(palette.accent))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@StringRes
private fun AppThemeId.labelRes(): Int = when (this) {
    AppThemeId.NIGHT -> R.string.theme_night
    AppThemeId.AURORA -> R.string.theme_aurora
    AppThemeId.SUNSET -> R.string.theme_sunset
    AppThemeId.ONYX -> R.string.theme_onyx
    AppThemeId.MOONLIGHT -> R.string.theme_moonlight
    AppThemeId.PAPER -> R.string.theme_paper
    AppThemeId.SAKURA -> R.string.theme_sakura
    AppThemeId.MINT -> R.string.theme_mint
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
