package jp.co.studio.kaka.domain.repository

import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.Music
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DownloadRepository {
    /** Single source of truth for per-music download status, merging WorkManager progress with the Room table. */
    val downloadStates: StateFlow<Map<Long, DownloadState>>
    val downloadedMusics: Flow<List<DownloadedMusic>>

    fun download(music: Music)
    suspend fun delete(musicId: Long)
    suspend fun reconcile()
}
