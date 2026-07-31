package jp.co.studio.kaka.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import jp.co.studio.kaka.domain.model.Category

@Composable
fun CategoryCard(category: Category, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = category.coverUrl,
            contentDescription = category.name,
            modifier = Modifier.fillMaxWidth().height(96.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(Color.Black.copy(alpha = 0.35f)),
        )
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        )
    }
}
