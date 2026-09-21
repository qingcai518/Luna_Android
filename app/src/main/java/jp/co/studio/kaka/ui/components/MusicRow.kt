package jp.co.studio.kaka.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Music

/** 歌曲封面：圆角方图；[isCurrent] 时盖一层半透明黑 + 声波，一眼能看出是哪首在播。 */
@Composable
fun SongCover(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false,
    size: Dp = 56.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (isCurrent) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

/**
 * 歌曲行，歌单 / 搜索 / 推荐共用。只传 [music] 和 [onClick] 时是最简形式；
 * [reason]（AI 推荐理由）、[detail]（时长·大小等）、[isCurrent]（正在播放）、[trailing]（行尾自定义内容）都是可选的。
 * 行尾的下载图标只在传了 [onDownloadClick] 时显示；传了 [trailing] 则以 [trailing] 为准。
 */
@Composable
fun MusicRow(
    music: Music,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    downloadState: DownloadState = DownloadState.NotDownloaded,
    onDownloadClick: (() -> Unit)? = null,
    reason: String? = null,
    detail: String? = null,
    isCurrent: Boolean = false,
    coverModel: Any? = music.coverUrl,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongCover(model = coverModel, contentDescription = music.title, isCurrent = isCurrent)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = music.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            val meta = listOfNotNull(music.artist?.name, music.category?.name).joinToString(" · ")
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!detail.isNullOrEmpty()) {
                Text(
                    text = detail,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
            if (!reason.isNullOrBlank()) {
                Row(modifier = Modifier.padding(top = 6.dp)) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 3.dp).size(12.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = reason,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    )
                }
            }
        }
        when {
            trailing != null -> trailing()
            onDownloadClick != null -> DownloadStateIcon(state = downloadState, onClick = onDownloadClick)
        }
    }
}
