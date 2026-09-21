package jp.co.studio.kaka.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** 头部里的一个操作按钮。[isBusy] 时按钮上显示转圈、不可点。 */
data class HeaderAction(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isBusy: Boolean = false,
    val enabled: Boolean = true,
)

/**
 * 列表页顶部的通用头部：标签 + 大标题 + 说明 + 至多两个可见的主 / 次操作。
 * 推荐页、已下载页共用（对应 iOS 的 PageHeaderView），取代 TopAppBar 里没有文字的图标按钮。
 */
@Composable
fun PageHeader(
    tagIcon: ImageVector,
    tagText: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    primary: HeaderAction? = null,
    secondary: HeaderAction? = null,
) {
    val accent = MaterialTheme.colorScheme.primary
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(tagIcon, contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(tagText, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = accent)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp),
        )
        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (primary != null || secondary != null) {
            Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                primary?.let { HeaderButton(it, prominent = true) }
                secondary?.let { HeaderButton(it, prominent = false) }
            }
        }
    }
}

@Composable
private fun HeaderButton(action: HeaderAction, prominent: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    // 主操作：强调色底 + 页面底色字（两套配色下对比度都 ≥ 4.5:1）；次操作：淡强调色底 + 强调色字
    val colors = if (prominent) {
        ButtonDefaults.buttonColors(containerColor = accent, contentColor = MaterialTheme.colorScheme.onPrimary)
    } else {
        ButtonDefaults.buttonColors(
            containerColor = accent.copy(alpha = 0.16f),
            contentColor = accent,
            disabledContainerColor = accent.copy(alpha = 0.16f),
            disabledContentColor = accent.copy(alpha = 0.6f),
        )
    }
    Button(
        onClick = action.onClick,
        enabled = action.enabled && !action.isBusy,
        colors = colors,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        modifier = Modifier.heightIn(min = 44.dp),
    ) {
        if (action.isBusy) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
        } else {
            Icon(action.icon, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(action.text, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}
