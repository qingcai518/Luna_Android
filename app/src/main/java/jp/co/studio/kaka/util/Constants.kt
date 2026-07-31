package jp.co.studio.kaka.util

object Constants {
    const val API_SUCCESS_CODE = "000000"

    const val AUTH_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "

    // Paths that must NOT get an Authorization header attached / must not trigger token refresh.
    val NO_AUTH_PATHS = setOf("users/login", "users/register", "users/refresh-token")

    const val SEARCH_DEBOUNCE_MS = 300L
    const val SEARCH_DEFAULT_PAGE = 1
    const val SEARCH_DEFAULT_SIZE = 20

    const val RECOMMENDATION_SCENE_HOME = "home"
    const val RECOMMENDATION_SCENE_RECOMMEND = "recommend"
    const val RECOMMENDATION_LIMIT_HOME = 10
    const val RECOMMENDATION_LIMIT_RECOMMEND = 20

    const val EVENT_BATCH_SIZE_THRESHOLD = 10
    const val EVENT_BATCH_FLUSH_INTERVAL_MS = 15_000L

    const val DOWNLOAD_DIR_MUSIC = "music"
    const val DOWNLOAD_DIR_COVERS = "covers"
    const val DOWNLOAD_DIR_LYRICS = "lyrics"

    const val DOWNLOAD_WORK_TAG = "luna_download"
    fun downloadWorkName(musicId: Long) = "download_music_$musicId"
}

enum class SearchType(val apiValue: String) {
    ALL("all"),
    MUSIC("music"),
    ARTIST("artist"),
    CATEGORY("category"),
    REGION("region"),
}

enum class MusicListSource {
    ARTIST,
    CATEGORY,
}
