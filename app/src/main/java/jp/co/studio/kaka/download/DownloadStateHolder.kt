package jp.co.studio.kaka.download

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin façade over [DownloadRepository] so SearchViewModel/MusicListViewModel/RecommendViewModel
 * all read the same download-state source instead of each re-implementing it (the iOS app
 * duplicates this logic across three screens - this is the fix for that).
 */
@Singleton
class DownloadStateHolder @Inject constructor(
    private val downloadRepository: DownloadRepository,
) {
    val states: StateFlow<Map<Long, DownloadState>> = downloadRepository.downloadStates

    fun download(music: Music) = downloadRepository.download(music)
}
