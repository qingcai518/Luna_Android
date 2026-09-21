package jp.co.studio.kaka.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.LyricLine
import jp.co.studio.kaka.player.PlayerRepeatMode
import jp.co.studio.kaka.player.PlayerUiState

@Composable
fun FullPlayerScreen(viewModel: FullPlayerViewModel = hiltViewModel()) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()

    FullPlayerContent(
        playerState = playerState,
        lyrics = lyrics,
        onSeek = viewModel::seekTo,
        onPlayPause = viewModel::playPause,
        onPrevious = viewModel::skipPrevious,
        onNext = viewModel::skipNext,
        onSetShuffle = viewModel::setShuffleEnabled,
        onSetRepeat = viewModel::setRepeatMode,
        onQueueItemClick = viewModel::seekToQueueItem,
    )
}

/**
 * 无状态的全屏播放页内容：所有数据和回调都从参数进来，方便预览和截图。
 * 自上而下：「正在播放」+ 队列入口 → 唱片（高度自适应）→ 标题/歌手 → 歌词（占剩余高度）→ 进度条 → 五键控制区。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FullPlayerContent(
    playerState: PlayerUiState,
    lyrics: List<LyricLine>,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSetShuffle: (Boolean) -> Unit,
    onSetRepeat: (PlayerRepeatMode) -> Unit,
    onQueueItemClick: (Int) -> Unit,
) {
    var showQueue by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableStateOf<Long?>(null) }
    val music = playerState.currentMusic

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 唱片按可用高度取值：矮屏幕自动缩小，给歌词留空间；最大 300dp
        val discSize = (maxHeight * 0.30f).coerceIn(140.dp, 300.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 顶部：居中的「正在播放」，队列入口在右侧
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.player_now_playing),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(onClick = { showQueue = true }, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = stringResource(R.string.player_queue))
                }
            }

            Box(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                AlbumArtDisc(coverUrl = music?.coverUrl, isPlaying = playerState.isPlaying, size = discSize)
            }

            Text(
                text = music?.title.orEmpty(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 16.dp),
            )
            music?.artist?.name?.let { artistName ->
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            // 歌词：占剩余高度，直接铺在页面上，离当前行越远越淡
            LyricsView(
                lines = lyrics,
                positionMs = playerState.positionMs,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .heightIn(min = 96.dp),
            )

            // 进度条：细轨道 + 小圆点，颜色跟主题
            val interactionSource = remember { MutableInteractionSource() }
            Slider(
                value = (dragPositionMs ?: playerState.positionMs).toFloat(),
                onValueChange = { value -> dragPositionMs = value.toLong() },
                onValueChangeFinished = {
                    dragPositionMs?.let(onSeek)
                    dragPositionMs = null
                },
                valueRange = 0f..playerState.durationMs.coerceAtLeast(1L).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                ),
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = interactionSource,
                        thumbSize = DpSize(14.dp, 14.dp),
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary),
                    )
                },
                // 默认轨道有 16dp 粗、带缺口和终点圆点；这里收成 4dp 细轨道
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(4.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                        ),
                        drawStopIndicator = null,
                        thumbTrackGapSize = 0.dp,
                        trackInsideCornerSize = 2.dp,
                    )
                },
                interactionSource = interactionSource,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatMs(dragPositionMs ?: playerState.positionMs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatMs(playerState.durationMs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // 控制区：随机 | 上一首 | 播放/暂停（主操作，实心强调色圆）| 下一首 | 循环
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 默认状态用弱化的次要色，用户主动打开的（随机 / 循环）才用强调色
                IconButton(onClick = { onSetShuffle(!playerState.shuffleEnabled) }) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = stringResource(R.string.player_shuffle),
                        tint = if (playerState.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = stringResource(R.string.player_previous), modifier = Modifier.size(32.dp))
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = MaterialTheme.colorScheme.primary, spotColor = MaterialTheme.colorScheme.primary)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(if (playerState.isPlaying) R.string.player_pause else R.string.player_play),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp),
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.player_next), modifier = Modifier.size(32.dp))
                }
                IconButton(
                    onClick = {
                        val next = when (playerState.repeatMode) {
                            PlayerRepeatMode.OFF -> PlayerRepeatMode.ALL
                            PlayerRepeatMode.ALL -> PlayerRepeatMode.ONE
                            PlayerRepeatMode.ONE -> PlayerRepeatMode.OFF
                        }
                        onSetRepeat(next)
                    },
                ) {
                    Icon(
                        imageVector = if (playerState.repeatMode == PlayerRepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = stringResource(R.string.player_repeat_mode),
                        tint = if (playerState.repeatMode != PlayerRepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showQueue) {
        QueueSheet(
            queue = playerState.queue,
            currentIndex = playerState.currentIndex,
            onItemClick = { index ->
                onQueueItemClick(index)
                showQueue = false
            },
            onDismissRequest = { showQueue = false },
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
