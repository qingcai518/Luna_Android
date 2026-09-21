package jp.co.studio.kaka.ui.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.isActive

/**
 * Continuously rotating cover art. Uses a cancellable [Animatable] loop rather than
 * `rememberInfiniteTransition` because pausing must freeze the disc at its *current* angle and
 * resuming must continue from there without a visual jump - `LaunchedEffect(isPlaying)` cancels
 * the animation coroutine when playback pauses, which leaves `rotation.value` exactly where it
 * was; restarting the effect resumes the +360 loop from that same value.
 *
 * 光晕和中心孔不跟着转（否则光晕会随旋转来回偏移）；只有封面图旋转。
 */
@Composable
fun AlbumArtDisc(coverUrl: String?, isPlaying: Boolean, modifier: Modifier = Modifier, size: Dp = 240.dp) {
    val rotation = remember { Animatable(0f) }
    val accent = MaterialTheme.colorScheme.primary

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(durationMillis = 8_000, easing = LinearEasing),
                )
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            // 唱片背后的光晕跟随主题强调色（和首页主角卡呼应）
            .shadow(elevation = 24.dp, shape = CircleShape, ambientColor = accent.copy(alpha = 0.4f), spotColor = accent.copy(alpha = 0.6f))
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation.value }
                .border(6.dp, Color.Black.copy(alpha = 0.12f), CircleShape),
        )
        // 中心孔
        Box(
            modifier = Modifier
                .size(size * 0.1f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .border(2.dp, Color.Black.copy(alpha = 0.12f), CircleShape),
        )
    }
}
