package jp.co.studio.kaka.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.player.PlayerRepeatMode

@Composable
fun FullPlayerScreen(viewModel: FullPlayerViewModel = hiltViewModel()) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    var showQueue by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableStateOf<Long?>(null) }

    val music = playerState.currentMusic

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AlbumArtDisc(coverUrl = music?.coverUrl, isPlaying = playerState.isPlaying)
        }

        Text(
            text = music?.title.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 24.dp),
        )
        music?.artist?.name?.let { artistName ->
            Text(artistName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LyricsView(
            lines = lyrics,
            positionMs = playerState.positionMs,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(top = 16.dp),
        )

        Slider(
            value = (dragPositionMs ?: playerState.positionMs).toFloat(),
            onValueChange = { value -> dragPositionMs = value.toLong() },
            onValueChangeFinished = {
                dragPositionMs?.let { viewModel.seekTo(it) }
                dragPositionMs = null
            },
            valueRange = 0f..playerState.durationMs.coerceAtLeast(1L).toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMs(dragPositionMs ?: playerState.positionMs), style = MaterialTheme.typography.labelSmall)
            Text(formatMs(playerState.durationMs), style = MaterialTheme.typography.labelSmall)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.setShuffleEnabled(!playerState.shuffleEnabled) }) {
                Icon(
                    Icons.Filled.Shuffle,
                    contentDescription = "随机播放",
                    tint = if (playerState.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = viewModel::skipPrevious) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "上一首", modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = viewModel::playPause, modifier = Modifier.size(64.dp)) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(48.dp),
                )
            }
            IconButton(onClick = viewModel::skipNext) {
                Icon(Icons.Filled.SkipNext, contentDescription = "下一首", modifier = Modifier.size(36.dp))
            }
            IconButton(
                onClick = {
                    val next = when (playerState.repeatMode) {
                        PlayerRepeatMode.OFF -> PlayerRepeatMode.ALL
                        PlayerRepeatMode.ALL -> PlayerRepeatMode.ONE
                        PlayerRepeatMode.ONE -> PlayerRepeatMode.OFF
                    }
                    viewModel.setRepeatMode(next)
                },
            ) {
                Icon(
                    imageVector = if (playerState.repeatMode == PlayerRepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    contentDescription = "循环模式",
                    tint = if (playerState.repeatMode != PlayerRepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        IconButton(onClick = { showQueue = true }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "播放队列")
        }
    }

    if (showQueue) {
        QueueSheet(
            queue = playerState.queue,
            currentIndex = playerState.currentIndex,
            onItemClick = { index ->
                viewModel.seekToQueueItem(index)
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
