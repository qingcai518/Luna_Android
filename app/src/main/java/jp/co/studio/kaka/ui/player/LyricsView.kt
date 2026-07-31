package jp.co.studio.kaka.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.co.studio.kaka.domain.model.LyricLine

@Composable
fun LyricsView(lines: List<LyricLine>, positionMs: Long, modifier: Modifier = Modifier) {
    if (lines.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无歌词", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val listState = rememberLazyListState()
    val currentIndex = remember(lines, positionMs) {
        var index = 0
        for (i in lines.indices) {
            if (lines[i].timeMs <= positionMs) index = i else break
        }
        index
    }

    LaunchedEffect(currentIndex) {
        listState.animateScrollToItem(index = currentIndex, scrollOffset = -200)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 160.dp),
    ) {
        itemsIndexed(lines) { index, line ->
            Text(
                text = line.text,
                textAlign = TextAlign.Center,
                style = if (index == currentIndex) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                color = if (index == currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 24.dp),
            )
        }
    }
}
