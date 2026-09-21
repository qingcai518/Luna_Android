package jp.co.studio.kaka.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.Artist
import jp.co.studio.kaka.domain.model.Category
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.ui.components.EmptyState
import jp.co.studio.kaka.ui.components.ErrorState
import jp.co.studio.kaka.ui.components.MusicRow
import jp.co.studio.kaka.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onArtistClick: (Artist) -> Unit,
    onCategoryClick: (Category) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.nav_search)) })
        OutlinedTextField(
            value = uiState.keyword,
            onValueChange = viewModel::onKeywordChange,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        when {
            uiState.errorMessage != null -> ErrorState(message = uiState.errorMessage!!.asString(), onRetry = {})
            uiState.isEmpty -> EmptyState(message = stringResource(R.string.search_no_results))
            else -> {
                val musics = uiState.result.musics.orEmpty()
                val artists = uiState.result.artists.orEmpty()
                val categories = uiState.result.categories.orEmpty()
                val regions = uiState.result.regions.orEmpty()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (musics.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.search_type_song)) }
                        items(musics, key = { "music_${it.id}" }) { music ->
                            MusicRow(
                                music = music,
                                onClick = { playerViewModel.playQueue(musics, musics.indexOf(music)) },
                                downloadState = uiState.downloadStates[music.id] ?: DownloadState.NotDownloaded,
                                onDownloadClick = { viewModel.downloadMusic(music) },
                                isCurrent = playerState.currentMusic?.id == music.id,
                            )
                        }
                    }
                    if (artists.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.home_artists)) }
                        items(artists, key = { "artist_${it.id}" }) { artist ->
                            SimpleResultRow(text = artist.name, onClick = { onArtistClick(artist) })
                        }
                    }
                    if (categories.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.home_categories)) }
                        items(categories, key = { "category_${it.id}" }) { category ->
                            SimpleResultRow(text = category.name, onClick = { onCategoryClick(category) })
                        }
                    }
                    if (regions.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.search_type_region)) }
                        // Region results are read-only - no click handler, matching iOS behavior.
                        items(regions, key = { it.regionCode ?: it.displayName }) { region ->
                            SimpleResultRow(text = region.displayName, onClick = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SimpleResultRow(text: String, onClick: (() -> Unit)?) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 16.dp, vertical = 12.dp)
    Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = rowModifier)
}
