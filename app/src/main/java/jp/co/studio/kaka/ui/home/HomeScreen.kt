package jp.co.studio.kaka.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.domain.model.Artist
import jp.co.studio.kaka.domain.model.Category
import jp.co.studio.kaka.ui.components.ArtistCard
import jp.co.studio.kaka.ui.components.CategoryCard
import jp.co.studio.kaka.ui.components.ErrorState
import jp.co.studio.kaka.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArtistClick: (Artist) -> Unit,
    onCategoryClick: (Category) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("主页") })
        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> ErrorState(message = uiState.errorMessage!!, onRetry = viewModel::load)
                else -> HomeContent(
                    artists = uiState.artists,
                    categories = uiState.categories,
                    onArtistClick = onArtistClick,
                    onCategoryClick = onCategoryClick,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    artists: List<Artist>,
    categories: List<Category>,
    onArtistClick: (Artist) -> Unit,
    onCategoryClick: (Category) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (artists.isNotEmpty()) {
            item {
                Text(
                    text = "歌手",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(artists, key = { it.id }) { artist ->
                        ArtistCard(artist = artist, onClick = { onArtistClick(artist) })
                    }
                }
            }
        }

        if (categories.isNotEmpty()) {
            item {
                Text(
                    text = "分类",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(categories.chunked(2)) { rowCategories ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowCategories.forEach { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onCategoryClick(category) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowCategories.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
