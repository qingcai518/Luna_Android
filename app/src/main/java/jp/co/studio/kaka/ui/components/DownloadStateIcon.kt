package jp.co.studio.kaka.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.DownloadState

/** Four-state trailing icon shared by every music row: not-downloaded / downloading / downloaded / failed. */
@Composable
fun DownloadStateIcon(state: DownloadState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    when (state) {
        is DownloadState.NotDownloaded -> IconButton(onClick = onClick, modifier = modifier) {
            // 未下载：弱化的次要色，只有「已下载」才用强调色——不再让每一行都亮着一个显眼的图标
            Icon(Icons.Filled.CloudDownload, contentDescription = stringResource(R.string.download_action), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        is DownloadState.Downloading -> Box(
            modifier = modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { state.progress / 100f },
                modifier = Modifier.size(24.dp),
            )
        }
        is DownloadState.Downloaded -> IconButton(onClick = {}, enabled = false, modifier = modifier) {
            Icon(Icons.Filled.CheckCircle, contentDescription = stringResource(R.string.download_done), tint = MaterialTheme.colorScheme.primary)
        }
        is DownloadState.Failed -> IconButton(onClick = onClick, modifier = modifier) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = stringResource(R.string.download_failed), tint = MaterialTheme.colorScheme.error)
        }
    }
}
