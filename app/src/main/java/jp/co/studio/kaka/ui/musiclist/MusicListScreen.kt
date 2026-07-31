package jp.co.studio.kaka.ui.musiclist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.ui.components.EmptyState
import jp.co.studio.kaka.ui.components.ErrorState
import jp.co.studio.kaka.ui.components.LoadingState
import jp.co.studio.kaka.ui.components.MusicRow
import jp.co.studio.kaka.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListScreen(
    onBackClick: () -> Unit,
    viewModel: MusicListViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(innerPadding))
            uiState.errorMessage != null ->
                ErrorState(message = uiState.errorMessage!!, onRetry = viewModel::load, modifier = Modifier.padding(innerPadding))
            uiState.musics.isEmpty() -> EmptyState(message = "暂无歌曲", modifier = Modifier.padding(innerPadding))
            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(uiState.musics, key = { it.id }) { music ->
                    MusicRow(
                        music = music,
                        onClick = {
                            val index = uiState.musics.indexOf(music)
                            playerViewModel.playQueue(uiState.musics, index)
                        },
                        downloadState = uiState.downloadStates[music.id] ?: DownloadState.NotDownloaded,
                        onDownloadClick = { viewModel.downloadMusic(music) },
                    )
                }
            }
        }
    }
}
