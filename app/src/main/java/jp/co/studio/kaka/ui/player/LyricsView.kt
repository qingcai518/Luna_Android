package jp.co.studio.kaka.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.LyricLine
import kotlin.math.abs

/**
 * Renders a small fixed window of lines centered on whichever one is currently playing, derived
 * directly from [positionMs] on every recomposition - deliberately NOT a scrolling LazyColumn.
 * An earlier LazyColumn + scrollToItem/animateScrollToItem implementation kept desyncing from
 * the real current line on-device (verified via logging: the scroll state's own bookkeeping
 * advanced correctly, but the actually painted lines lagged behind and stayed stuck), because it
 * introduced a second, separately-updated source of truth (scroll position) that could fall out
 * of step with [currentIndex]. Rendering the window straight from currentIndex each time removes
 * that failure mode entirely - there is nothing left to desync.
 *
 * 外观：直接铺在页面上（不放卡片里），当前行强调色加粗，离当前行越远越淡——效果上等同于上下边缘渐隐，
 * 但不需要离屏合成，也不会引入第二个状态来源。
 */
@Composable
fun LyricsView(lines: List<LyricLine>, positionMs: Long, modifier: Modifier = Modifier) {
    if (lines.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.lyrics_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val currentIndex = remember(lines, positionMs) {
        var index = 0
        for (i in lines.indices) {
            if (lines[i].timeMs <= positionMs) index = i else break
        }
        index
    }

    Column(
        modifier = modifier.fillMaxSize().clipToBounds(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (i in (currentIndex - 2)..(currentIndex + 2)) {
            val line = lines.getOrNull(i) ?: continue
            val distance = abs(i - currentIndex)
            val isCurrent = distance == 0
            Text(
                text = line.text,
                textAlign = TextAlign.Center,
                style = if (isCurrent) {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = if (isCurrent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = if (distance == 1) 0.5f else 0.25f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 24.dp),
            )
        }
    }
}
