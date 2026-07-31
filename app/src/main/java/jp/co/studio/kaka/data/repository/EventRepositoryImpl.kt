package jp.co.studio.kaka.data.repository

import jp.co.studio.kaka.data.remote.api.ContentApiService
import jp.co.studio.kaka.data.remote.dto.EventTypeDto
import jp.co.studio.kaka.data.remote.dto.UserEventDto
import jp.co.studio.kaka.domain.repository.EventRepository
import jp.co.studio.kaka.util.safeApiCall
import kotlinx.serialization.json.Json
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val apiService: ContentApiService,
    private val json: Json,
) : EventRepository {

    override suspend fun trackSkip(musicId: Long, source: String) {
        report(UserEventDto(eventType = EventTypeDto.SKIP, musicId = musicId, source = source))
    }

    override suspend fun trackSearch(keyword: String, source: String) {
        report(UserEventDto(eventType = EventTypeDto.SEARCH, keyword = keyword, source = source))
    }

    override suspend fun trackDownload(musicId: Long, source: String) {
        report(UserEventDto(eventType = EventTypeDto.DOWNLOAD, musicId = musicId, source = source))
    }

    override suspend fun trackPlay(musicId: Long, playDurationSeconds: Int, totalDurationSeconds: Int?, source: String) {
        report(
            UserEventDto(
                eventType = EventTypeDto.PLAY,
                musicId = musicId,
                playDuration = playDurationSeconds,
                totalDuration = totalDurationSeconds,
                source = source,
            ),
        )
    }

    private suspend fun report(event: UserEventDto) {
        // Best-effort: a failed report is silently dropped, matching iOS's non-retrying behavior.
        safeApiCall(json) { apiService.postEventsBatch(listOf(event)) }
    }
}
