package jp.co.studio.kaka.ui.recommend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.ui.components.EmptyState
import jp.co.studio.kaka.ui.components.ErrorState
import jp.co.studio.kaka.ui.components.LoadingState
import jp.co.studio.kaka.ui.components.MusicRow
import jp.co.studio.kaka.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(
    viewModel: RecommendViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("推荐") },
            actions = {
                IconButton(onClick = viewModel::changeBatch) {
                    Icon(Icons.Filled.Refresh, contentDescription = "换一批")
                }
            },
        )
        when {
            uiState.isLoading -> LoadingState()
            uiState.errorMessage != null ->
                ErrorState(message = uiState.errorMessage!!, onRetry = viewModel::changeBatch)
            uiState.recommendations.isEmpty() -> EmptyState(message = "暂无推荐")
            else -> PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.recommendations, key = { it.music.id }) { recommendation ->
                        RecommendationRow(
                            recommendation = recommendation,
                            downloadState = uiState.downloadStates[recommendation.music.id] ?: DownloadState.NotDownloaded,
                            onClick = {
                                val musics = uiState.recommendations.map { it.music }
                                playerViewModel.playQueue(musics, musics.indexOf(recommendation.music))
                            },
                            onDownloadClick = { viewModel.downloadMusic(recommendation.music) },
                            onDismiss = { viewModel.dismiss(recommendation) },
                        )
                    }
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
        Column {
            MusicRow(
                music = recommendation.music,
                onClick = onClick,
                downloadState = downloadState,
                onDownloadClick = onDownloadClick,
            )
            recommendation.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 76.dp, end = 16.dp, bottom = 8.dp),
                )
            }
        }
    }
}
