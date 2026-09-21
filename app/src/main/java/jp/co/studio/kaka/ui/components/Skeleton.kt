package jp.co.studio.kaka.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 骨架屏的「流光」背景：一条高光从左扫到右，循环。 */
@Composable
fun Modifier.shimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1400, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f).compositeOver(base)
    val start = -600f + progress * 1800f
    return background(Brush.horizontalGradient(listOf(base, highlight, base), startX = start, endX = start + 600f))
}

@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(12.dp)) {
    Box(modifier = modifier.clip(shape).shimmer())
}

/** 歌曲行的占位：封面 + 三条文字。 */
@Composable
fun SongRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBlock(modifier = Modifier.size(56.dp))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            SkeletonBlock(modifier = Modifier.width(160.dp).size(width = 160.dp, height = 16.dp), shape = RoundedCornerShape(6.dp))
            SkeletonBlock(modifier = Modifier.padding(top = 8.dp).size(width = 100.dp, height = 12.dp), shape = RoundedCornerShape(6.dp))
            SkeletonBlock(modifier = Modifier.padding(top = 10.dp).size(width = 220.dp, height = 12.dp), shape = RoundedCornerShape(6.dp))
        }
    }
}

/** 圆形头像占位（歌手行用）。 */
@Composable
fun AvatarSkeleton(size: Dp, modifier: Modifier = Modifier) {
    SkeletonBlock(modifier = modifier.size(size), shape = RoundedCornerShape(size / 2))
}
