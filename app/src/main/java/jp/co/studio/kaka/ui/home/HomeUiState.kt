package jp.co.studio.kaka.ui.home

import jp.co.studio.kaka.domain.model.Artist
import jp.co.studio.kaka.domain.model.Category
import jp.co.studio.kaka.domain.model.Recommendation
import jp.co.studio.kaka.util.UiText

data class HomeUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val categories: List<Category> = emptyList(),
    val errorMessage: UiText? = null,
    /** 主角卡用的推荐（整批，点播放键时整批当播放队列）。跟艺术家/分类分开加载：AI 推荐可能很慢。 */
    val heroItems: List<Recommendation> = emptyList(),
    val isHeroLoading: Boolean = true,
    val downloadedCount: Int = 0,
) {
    val hero: Recommendation? get() = heroItems.firstOrNull()
}
