package jp.co.studio.kaka.ui.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.isActive

/**
 * Continuously rotating cover art. Uses a cancellable [Animatable] loop rather than
 * `rememberInfiniteTransition` because pausing must freeze the disc at its *current* angle and
 * resuming must continue from there without a visual jump - `LaunchedEffect(isPlaying)` cancels
 * the animation coroutine when playback pauses, which leaves `rotation.value` exactly where it
 * was; restarting the effect resumes the +360 loop from that same value.
 */
@Composable
fun AlbumArtDisc(coverUrl: String?, isPlaying: Boolean, modifier: Modifier = Modifier) {
    val rotation = remember { Animatable(0f) }

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

    AsyncImage(
        model = coverUrl,
        contentDescription = null,
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .graphicsLayer { rotationZ = rotation.value },
    )
}
