package jp.co.studio.kaka.ui.downloaded

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.toMusic
import jp.co.studio.kaka.ui.components.EmptyState
import jp.co.studio.kaka.ui.components.HeaderAction
import jp.co.studio.kaka.ui.components.MusicRow
import jp.co.studio.kaka.ui.components.PageHeader
import jp.co.studio.kaka.ui.components.SongRowSkeleton
import jp.co.studio.kaka.ui.player.PlayerViewModel
import java.io.File

@Composable
fun DownloadedScreen(
    viewModel: DownloadedViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        DownloadedContent(
            state = uiState,
            currentMusicId = playerState.currentMusic?.id,
            onPlayAll = { viewModel.play(uiState.musics, 0) },
            // 随机播放只打乱这一次的播放队列，不去改全局的随机开关
            onShuffle = { viewModel.play(uiState.musics.shuffled(), 0) },
            onRowClick = { music -> viewModel.play(uiState.musics, uiState.musics.indexOf(music)) },
            onDeleteRequest = { pendingDelete = it.id },
        )
    }

    val deleteId = pendingDelete
    if (deleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.downloaded_delete_title)) },
            text = { Text(stringResource(R.string.downloaded_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(deleteId)
                    pendingDelete = null
                }) { Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

/** 无状态的已下载页内容：所有数据和回调都从参数进来，方便预览和截图。 */
@Composable
internal fun DownloadedContent(
    state: DownloadedUiState,
    currentMusicId: Long?,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onRowClick: (DownloadedMusic) -> Unit,
    onDeleteRequest: (DownloadedMusic) -> Unit,
) {
    val context = LocalContext.current
    // 每首歌的文件大小；头部的总大小也用它
    val sizes = remember(state.musics) { state.musics.associate { it.id to File(it.localAudioPath).length() } }
    val hasContent = state.musics.isNotEmpty()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            PageHeader(
                tagIcon = Icons.Filled.CloudDownload,
                tagText = stringResource(R.string.downloaded_offline_tag),
                title = stringResource(R.string.nav_downloaded),
                subtitle = if (hasContent) {
                    stringResource(R.string.downloaded_summary, state.musics.size, Formatter.formatShortFileSize(context, sizes.values.sum()))
                } else null,
                primary = if (hasContent) HeaderAction(stringResource(R.string.common_play_all), Icons.Filled.PlayArrow, onPlayAll) else null,
                secondary = if (hasContent) HeaderAction(stringResource(R.string.common_shuffle), Icons.Filled.Shuffle, onShuffle) else null,
            )
        }
        when {
            state.isLoading -> items(4) { SongRowSkeleton() }
            !hasContent -> item {
                Box(modifier = Modifier.padding(top = 48.dp)) { EmptyState(message = stringResource(R.string.downloaded_empty)) }
            }
            else -> items(state.musics, key = { it.id }) { music ->
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                    MusicRow(
                        music = music.toMusic(),
                        onClick = { onRowClick(music) },
                        detail = detailText(context, music, sizes[music.id]),
                        isCurrent = currentMusicId == music.id,
                        // 删除收进右侧的「⋯」菜单，不再靠长按（不可发现），也不摆一个醒目的删除图标
                        trailing = { RowMenu(onDelete = { onDeleteRequest(music) }) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 84.dp).align(Alignment.BottomStart),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RowMenu(onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.common_more),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

/** 「时长 · 大小」；两者都缺时返回 null。 */
private fun detailText(context: android.content.Context, music: DownloadedMusic, sizeBytes: Long?): String? {
    val parts = mutableListOf<String>()
    music.durationSeconds?.takeIf { it > 0 }?.let { parts += "%d:%02d".format(it / 60, it % 60) }
    sizeBytes?.takeIf { it > 0 }?.let { parts += Formatter.formatShortFileSize(context, it) }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
