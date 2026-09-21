package jp.co.studio.kaka.ui.recommend

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.ui.components.EmptyState
import jp.co.studio.kaka.ui.components.ErrorState
import jp.co.studio.kaka.ui.components.HeaderAction
import jp.co.studio.kaka.ui.components.MusicRow
import jp.co.studio.kaka.ui.components.PageHeader
import jp.co.studio.kaka.ui.components.SongRowSkeleton
import jp.co.studio.kaka.ui.player.PlayerViewModel
import jp.co.studio.kaka.util.UiText

@Composable
fun RecommendScreen(
    viewModel: RecommendViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 一次性提示（如「暂时没有新的推荐」）
    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            val text = when (message) {
                is UiText.Dynamic -> message.value
                is UiText.Resource -> context.getString(message.resId)
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        if (uiState.errorMessage != null) {
            ErrorState(message = uiState.errorMessage!!.asString(), onRetry = viewModel::changeBatch)
        } else {
            RecommendContent(
                state = uiState,
                currentMusicId = playerState.currentMusic?.id,
                onPlayAll = {
                    val musics = uiState.recommendations.map { it.music }
                    if (musics.isNotEmpty()) playerViewModel.playQueue(musics, 0)
                },
                onRefreshClick = viewModel::changeBatch,
                onPullRefresh = viewModel::refresh,
                onRowClick = { recommendation ->
                    val musics = uiState.recommendations.map { it.music }
                    playerViewModel.playQueue(musics, musics.indexOf(recommendation.music))
                },
                onDownloadClick = { viewModel.downloadMusic(it.music) },
                onDismiss = viewModel::dismiss,
            )
        }
    }
}

/** 无状态的推荐页内容：所有数据和回调都从参数进来，方便预览和截图。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecommendContent(
    state: RecommendUiState,
    currentMusicId: Long?,
    onPlayAll: () -> Unit,
    onRefreshClick: () -> Unit,
    onPullRefresh: () -> Unit,
    onRowClick: (Recommendation) -> Unit,
    onDownloadClick: (Recommendation) -> Unit,
    onDismiss: (Recommendation) -> Unit,
) {
    // 「播放全部」只在有歌可播时显示；首次加载中「换一批」不可点，换一批进行中按钮转圈
    val canPlay = state.recommendations.isNotEmpty() && !state.isLoading
    val header: @Composable () -> Unit = {
        PageHeader(
            tagIcon = Icons.Filled.AutoAwesome,
            tagText = stringResource(R.string.home_hero_tag),
            title = stringResource(R.string.recommend_title),
            subtitle = stringResource(if (state.isLoading) R.string.recommend_loading else R.string.recommend_subtitle),
            primary = if (canPlay) HeaderAction(stringResource(R.string.common_play_all), Icons.Filled.PlayArrow, onPlayAll) else null,
            secondary = HeaderAction(
                text = stringResource(R.string.recommend_refresh),
                icon = Icons.Filled.Refresh,
                onClick = onRefreshClick,
                isBusy = state.isChangingBatch || state.isRefreshing,
                enabled = !state.isLoading,
            ),
        )
    }

    when {
        // 首次加载：骨架屏。AI 推荐在后端缓存未命中时要等大模型返回，可能要好几秒
        state.isLoading -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { header() }
            items(6) { SongRowSkeleton() }
        }
        state.recommendations.isEmpty() -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { header() }
            item { Box(modifier = Modifier.padding(top = 48.dp)) { EmptyState(message = stringResource(R.string.recommend_empty)) } }
        }
        else -> PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onPullRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { header() }
                items(state.recommendations, key = { it.music.id }) { recommendation ->
                    RecommendationRow(
                        recommendation = recommendation,
                        downloadState = state.downloadStates[recommendation.music.id] ?: DownloadState.NotDownloaded,
                        isCurrent = currentMusicId == recommendation.music.id,
                        onClick = { onRowClick(recommendation) },
                        onDownloadClick = { onDownloadClick(recommendation) },
                        onDismiss = { onDismiss(recommendation) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationRow(
    recommendation: Recommendation,
    downloadState: DownloadState,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            val dismissed = value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd
            if (dismissed) onDismiss()
            dismissed
        },
    )
    SwipeToDismissBox(state = dismissState, backgroundContent = {}) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            MusicRow(
                music = recommendation.music,
                onClick = onClick,
                downloadState = downloadState,
                onDownloadClick = onDownloadClick,
                reason = recommendation.reason,
                isCurrent = isCurrent,
            )
            // 分割线从文字左侧开始（16 边距 + 56 封面 + 12 间距）
            HorizontalDivider(
                modifier = Modifier.padding(start = 84.dp).align(androidx.compose.ui.Alignment.BottomStart),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}
