package jp.co.studio.kaka.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Music

/**
 * Reusable song row shared across MusicList/Search/Recommend - the trailing download icon is
 * only shown when [onDownloadClick] is provided, so screens that don't need it stay simple.
 */
@Composable
fun MusicRow(
    music: Music,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    downloadState: DownloadState = DownloadState.NotDownloaded,
    onDownloadClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = music.coverUrl,
            contentDescription = music.title,
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = music.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            music.artist?.name?.let { artistName ->
                Text(
                    text = artistName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (onDownloadClick != null) {
            DownloadStateIcon(state = downloadState, onClick = onDownloadClick)
        }
    }
}
