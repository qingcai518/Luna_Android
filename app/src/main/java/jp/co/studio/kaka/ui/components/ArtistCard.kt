package jp.co.studio.kaka.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jp.co.studio.kaka.domain.model.Artist

/** 首页艺术家：80dp 圆形头像 + 名字。人用圆、分类用方，形状本身就在区分两类内容。 */
@Composable
fun ArtistCard(artist: Artist, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(88.dp)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarCircle(
            seed = artist.id,
            name = artist.name,
            imageUrl = artist.avatarUrl,
            size = 80.dp,
            ring = 1.dp to MaterialTheme.colorScheme.outline,
        )
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
