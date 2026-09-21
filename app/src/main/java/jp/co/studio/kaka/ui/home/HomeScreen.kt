package jp.co.studio.kaka.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import jp.co.studio.kaka.R
import jp.co.studio.kaka.domain.model.Artist
import jp.co.studio.kaka.domain.model.Category
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.ui.components.ArtistCard
import jp.co.studio.kaka.ui.components.AvatarSkeleton
import jp.co.studio.kaka.ui.components.CategoryCard
import jp.co.studio.kaka.ui.components.ErrorState
import jp.co.studio.kaka.ui.components.SkeletonBlock
import jp.co.studio.kaka.ui.player.PlayerViewModel
import jp.co.studio.kaka.ui.theme.HeroAccent
import jp.co.studio.kaka.ui.theme.HeroBottomDark
import jp.co.studio.kaka.ui.theme.HeroBottomLight
import jp.co.studio.kaka.ui.theme.HeroInk
import jp.co.studio.kaka.ui.theme.HeroTopDark
import jp.co.studio.kaka.ui.theme.HeroTopLight
import jp.co.studio.kaka.ui.theme.isDark
import java.util.Calendar

@Composable
fun HomeScreen(
    onArtistClick: (Artist) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onNavigateToDownloaded: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        if (uiState.errorMessage != null) {
            ErrorState(message = uiState.errorMessage!!.asString(), onRetry = viewModel::retry)
        } else {
            HomeContent(
                state = uiState,
                currentMusicId = playerState.currentMusic?.id,
                isPlaying = playerState.isPlaying,
                onHeroPlayClick = {
                    val hero = uiState.hero ?: return@HomeContent
                    if (playerState.currentMusic?.id == hero.music.id) {
                        playerViewModel.playPause()
                    } else {
                        // 整批推荐当播放队列：播完第一首会接着放后面的
                        playerViewModel.playQueue(uiState.heroItems.map { it.music }, 0)
                    }
                },
                onDownloadsClick = onNavigateToDownloaded,
                onArtistClick = onArtistClick,
                onCategoryClick = onCategoryClick,
            )
        }
    }
}

/** 无状态的首页内容：所有数据和回调都从参数进来，方便预览和截图。 */
@Composable
internal fun HomeContent(
    state: HomeUiState,
    currentMusicId: Long?,
    isPlaying: Boolean,
    onHeroPlayClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onArtistClick: (Artist) -> Unit,
    onCategoryClick: (Category) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item { Greeting() }

        // 主角卡：AI 推荐的第一首歌。单独加载，慢或失败都不影响下面的内容
        if (state.isHeroLoading) {
            item { SkeletonBlock(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(264.dp), shape = RoundedCornerShape(28.dp)) }
        } else {
            state.hero?.let { hero ->
                item {
                    HeroCard(
                        hero = hero,
                        isPlaying = isPlaying && currentMusicId == hero.music.id,
                        onPlayClick = onHeroPlayClick,
                    )
                }
            }
        }

        if (state.downloadedCount > 0) {
            item { DownloadsTile(count = state.downloadedCount, onClick = onDownloadsClick) }
        }

        if (state.isLoading || state.artists.isNotEmpty()) {
            item {
                Column {
                    SectionTitle(stringResource(R.string.home_artists))
                    if (state.isLoading && state.artists.isEmpty()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            repeat(4) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AvatarSkeleton(size = 80.dp)
                                    SkeletonBlock(modifier = Modifier.padding(top = 8.dp).size(width = 56.dp, height = 14.dp), shape = RoundedCornerShape(6.dp))
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.artists, key = { it.id }) { artist ->
                                ArtistCard(artist = artist, onClick = { onArtistClick(artist) })
                            }
                        }
                    }
                }
            }
        }

        if (state.isLoading || state.categories.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(stringResource(R.string.home_categories))
                    if (state.isLoading && state.categories.isEmpty()) {
                        repeat(2) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(2) { SkeletonBlock(modifier = Modifier.weight(1f).height(96.dp), shape = RoundedCornerShape(20.dp)) }
                            }
                        }
                    } else {
                        state.categories.chunked(2).forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                rowCategories.forEach { category ->
                                    CategoryCard(category = category, onClick = { onCategoryClick(category) }, modifier = Modifier.weight(1f))
                                }
                                if (rowCategories.size == 1) Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
    )
}

/** 按时段变化的问候语。 */
@Composable
private fun Greeting() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingRes = when (hour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..17 -> R.string.home_greeting_afternoon
        in 18..22 -> R.string.home_greeting_evening
        else -> R.string.home_greeting_night
    }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(greetingRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.home_greeting_prompt),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
        )
    }
}

/**
 * 主角卡：始终是深色渐变、白色文字（浅色主题下也是），强调色固定用金色。
 * 首页唯一的大字、大图、强调色按钮，播放键在右下角，一步开始。
 */
@Composable
private fun HeroCard(hero: Recommendation, isPlaying: Boolean, onPlayClick: () -> Unit) {
    val dark = MaterialTheme.colorScheme.isDark
    val top = if (dark) HeroTopDark else HeroTopLight
    val bottom = if (dark) HeroBottomDark else HeroBottomLight
    val music = hero.music

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .heightIn(min = 264.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(top, bottom)))
            .clickable(onClick = onPlayClick),
    ) {
        // 右上角的光晕
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-48).dp)
                .size(220.dp)
                .background(Brush.radialGradient(listOf(HeroAccent.copy(alpha = 0.5f), Color.Transparent)), CircleShape),
        )
        // 斜放的封面
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(96.dp)
                .rotate(6f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.10f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(24.dp)),
        ) {
            if (!music.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = music.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(96.dp),
                )
            }
        }

        // 文字区：上方留给封面，右侧留给播放键
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 92.dp, top = 128.dp, bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = HeroAccent, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.home_hero_tag),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = HeroAccent,
                )
            }
            Text(
                text = music.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 12.dp),
            )
            val meta = listOfNotNull(music.artist?.name, music.category?.name).joinToString(" · ")
            if (meta.isNotEmpty()) {
                Text(text = meta, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.74f), modifier = Modifier.padding(top = 4.dp))
            }
            hero.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.74f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(HeroAccent)
                .clickable(onClick = onPlayClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play),
                tint = HeroInk,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

/** 「我的下载」快捷入口：数据全在本地，没有下载时整块不显示。 */
@Composable
private fun DownloadsTile(count: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = stringResource(R.string.home_downloads_title),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(R.string.home_downloads_subtitle, count),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}
