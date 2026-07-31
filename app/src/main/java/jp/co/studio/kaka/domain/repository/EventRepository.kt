package jp.co.studio.kaka.domain.repository

/** Fire-and-forget analytics events - a failed report is dropped, not retried (matches iOS). */
interface EventRepository {
    suspend fun trackSkip(musicId: Long, source: String)
    suspend fun trackSearch(keyword: String, source: String)
    suspend fun trackDownload(musicId: Long, source: String)

    /** Reports actual accumulated listening seconds for a track, not just "playback started". */
    suspend fun trackPlay(musicId: Long, playDurationSeconds: Int, totalDurationSeconds: Int?, source: String)
}
