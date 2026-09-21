package jp.co.studio.kaka.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.R
import jp.co.studio.kaka.ui.components.AvatarCircle

@Composable
fun ProfileScreen(
    onNavigateToDownloaded: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        state = uiState,
        onNavigateToDownloaded = onNavigateToDownloaded,
        onNavigateToSettings = onNavigateToSettings,
        onLogout = viewModel::logout,
    )
}

/** 无状态的「我的」页内容：所有数据和回调都从参数进来，方便预览和截图。 */
@Composable
internal fun ProfileContent(
    state: ProfileUiState,
    onNavigateToDownloaded: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    var confirmLogout by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // 头像 + 用户名 + 邮箱：没有头像图时用「按用户取色相的渐变 + 首字」，外圈是强调色
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarCircle(
                    seed = (state.username ?: "").hashCode().toLong(),
                    name = state.username ?: "?",
                    imageUrl = state.avatarUrl,
                    size = 72.dp,
                    ring = 2.dp to MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = state.username ?: stringResource(R.string.profile_not_logged_in),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    state.email?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            // 已下载歌曲 / 设置：一张圆角分组卡片，行间是细分割线
            val shape = RoundedCornerShape(16.dp)
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape),
            ) {
                ProfileRow(
                    icon = Icons.Filled.CloudDownload,
                    title = stringResource(R.string.profile_downloaded_songs),
                    value = "${state.downloadedCount}",
                    onClick = onNavigateToDownloaded,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = MaterialTheme.colorScheme.outline)
                ProfileRow(
                    icon = Icons.Filled.Settings,
                    title = stringResource(R.string.settings_title),
                    value = null,
                    onClick = onNavigateToSettings,
                )
            }
        }

        // 退出登录放在底部（拇指区），红色文字 + 淡红边，不抢主操作的风头；点了要确认
        OutlinedButton(
            onClick = { confirmLogout = true },
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.error,
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 52.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.profile_logout), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(stringResource(R.string.profile_logout_confirm_title)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    onLogout()
                }) { Text(stringResource(R.string.profile_logout), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

/** 分组卡片里的一行：浅色图标方块 + 标题 + 右侧当前值 + 箭头。 */
@Composable
private fun ProfileRow(icon: ImageVector, title: String, value: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
        )
        if (value != null) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
