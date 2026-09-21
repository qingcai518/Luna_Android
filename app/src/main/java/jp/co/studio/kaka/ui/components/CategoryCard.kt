package jp.co.studio.kaka.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import jp.co.studio.kaka.domain.model.Category
import jp.co.studio.kaka.ui.theme.isDark

/**
 * 首页分类：色彩卡片。色相由分类 id 派生（同一分类颜色稳定），封面斜切在右下角。
 * 深色主题：低亮度色相渐变 + 白字；浅色主题：浅色渐变 + 主题的文字色（深色）。
 */
@Composable
fun CategoryCard(category: Category, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hue = hueOf(category.id)
    val dark = MaterialTheme.colorScheme.isDark
    val brush = if (dark) {
        Brush.linearGradient(listOf(Color.hsv(hue, 0.50f, 0.36f), Color.hsv(hue, 0.55f, 0.22f)))
    } else {
        Brush.linearGradient(listOf(Color.hsv(hue, 0.12f, 0.98f), Color.hsv(hue, 0.20f, 0.92f)))
    }
    val ink = if (dark) Color.White else MaterialTheme.colorScheme.onBackground
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(shape)
            .background(brush)
            .border(1.dp, ink.copy(alpha = 0.06f), shape)
            .clickable(onClick = onClick),
    ) {
        // 斜切的封面在右下角，超出卡片的部分被裁掉；封面加载失败时只留一块淡色底
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 12.dp, y = 14.dp)
                .size(64.dp)
                .rotate(18f)
                .clip(RoundedCornerShape(16.dp))
                .background(ink.copy(alpha = 0.10f)),
        ) {
            if (!category.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = category.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 56.dp)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            category.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = ink.copy(alpha = 0.66f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
