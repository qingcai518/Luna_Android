package jp.co.studio.kaka.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlin.math.abs

/** 同一个 id 永远得到同一个色相（137 接近黄金角，相邻 id 的颜色差得开）；与 iOS、小程序的取色规则一致。 */
fun hueOf(id: Long): Float = ((abs(id) * 137) % 360).toFloat()

/**
 * 圆形头像：有图片就显示图；没有（或加载失败）就用「按 id 取色相的渐变 + 名字首字」。
 * [ring] 非空时画一圈描边。
 */
@Composable
fun AvatarCircle(
    seed: Long,
    name: String,
    imageUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    ring: Pair<Dp, Color>? = null,
) {
    val hue = hueOf(seed)
    var base = modifier
        .size(size)
        .clip(CircleShape)
        .background(Brush.linearGradient(listOf(Color.hsv(hue, 0.55f, 0.78f), Color.hsv(hue, 0.62f, 0.46f))))
    if (ring != null) base = base.border(ring.first, ring.second, CircleShape)
    Box(modifier = base, contentAlignment = Alignment.Center) {
        val initial = name.trim().firstOrNull()?.uppercase().orEmpty()
        if (initial.isNotEmpty()) {
            Text(text = initial, color = Color.White, fontSize = (size.value * 0.36f).sp, fontWeight = FontWeight.SemiBold)
        }
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
