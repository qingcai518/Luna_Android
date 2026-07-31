package jp.co.studio.kaka.data.repository

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.co.studio.kaka.data.local.db.dao.DownloadedMusicDao
import jp.co.studio.kaka.data.local.files.DownloadFileManager
import jp.co.studio.kaka.data.mapper.toDomain
import jp.co.studio.kaka.domain.model.DownloadState
import jp.co.studio.kaka.domain.model.DownloadedMusic
import jp.co.studio.kaka.domain.model.Music
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.download.DownloadWorker
import jp.co.studio.kaka.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val dao: DownloadedMusicDao,
    private val fileManager: DownloadFileManager,
) : DownloadRepository {

    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val downloadedMusics: Flow<List<DownloadedMusic>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override val downloadStates: StateFlow<Map<Long, DownloadState>> = combine(
        dao.observeAll(),
        workManager.getWorkInfosByTagFlow(Constants.DOWNLOAD_WORK_TAG),
    ) { downloaded, workInfos ->
        val states = mutableMapOf<Long, DownloadState>()
        downloaded.forEach { entity -> states[entity.id] = DownloadState.Downloaded(entity.toDomain()) }
        workInfos.forEach { info ->
            val musicId = musicIdFromTags(info) ?: return@forEach
            if (states.containsKey(musicId)) return@forEach
            states[musicId] = when (info.state) {
                WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED ->
                    DownloadState.Downloading(info.progress.getInt(DownloadWorker.KEY_PROGRESS, 0))
                WorkInfo.State.FAILED -> DownloadState.Failed
                else -> DownloadState.NotDownloaded
            }
        }
        states.toMap()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    override fun download(music: Music) {
        val inputData = DownloadWorker.buildInputData(
            musicId = music.id,
            audioUrl = music.audioUrl,
            coverUrl = music.coverUrl,
            title = music.title,
            artistName = music.artist?.name,
            categoryName = music.category?.name,
            releaseDate = music.releaseDate,
            durationSeconds = music.durationSeconds,
        )
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .addTag(Constants.DOWNLOAD_WORK_TAG)
            .addTag(musicTag(music.id))
            .build()
        workManager.enqueueUniqueWork(Constants.downloadWorkName(music.id), ExistingWorkPolicy.KEEP, request)
    }

    override suspend fun delete(musicId: Long) {
        val entity = dao.getById(musicId) ?: return
        File(entity.localAudioPath).delete()
        entity.localCoverPath?.let { File(it).delete() }
        entity.localLyricsPath?.let { File(it).delete() }
        dao.deleteById(musicId)
    }

    override suspend fun reconcile() = fileManager.reconcile()

    private fun musicTag(musicId: Long) = "music_$musicId"

    private fun musicIdFromTags(info: WorkInfo): Long? =
        info.tags.firstOrNull { it.startsWith("music_") }?.removePrefix("music_")?.toLongOrNull()
}
