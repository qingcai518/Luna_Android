package jp.co.studio.kaka.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.ui.components.SongCover

/**
 * 迷你播放条：封面 + 歌名/歌手 + 播放暂停（强调色，主操作）+ 下一首（次要色）+ 底部 2dp 播放进度。
 * [progress] 是 0..1 的播放进度。
 */
@Composable
fun MiniPlayerBar(
    music: Music,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    onNextClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        // 顶部 2dp 进度条：不用打开完整播放器也能看到播到哪了
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBarClick)
                .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongCover(model = music.coverUrl, contentDescription = music.title, size = 44.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = music.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
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
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            if (onNextClick != null) {
                IconButton(onClick = onNextClick) {
                    Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.player_next))
                }
            }
        }
    }
}
