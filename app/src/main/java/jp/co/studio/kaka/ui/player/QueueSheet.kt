package jp.co.studio.kaka.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.co.studio.kaka.domain.model.Music

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    queue: List<Music>,
    currentIndex: Int,
    onItemClick: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        LazyColumn {
            itemsIndexed(queue, key = { index, music -> "${music.id}_$index" }) { index, music ->
                Text(
                    text = (if (index == currentIndex) "▶ " else "") + music.title,
                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(index) }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
        }
    }
}
