package jp.co.studio.kaka.ui.navigation

object AuthRoutes {
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
}

object MainRoutes {
    const val HOME = "main/home"
    const val SEARCH = "main/search"
    const val DOWNLOADED = "main/downloaded"
    const val RECOMMEND = "main/recommend"
    const val PROFILE = "main/profile"

    /** idType is "artist" or "category" - matches jp.co.studio.kaka.util.MusicListSource name (lowercase). */
    const val MUSIC_LIST = "main/musicList/{idType}/{id}/{name}"

    fun musicList(idType: String, id: Long, name: String): String =
        "main/musicList/$idType/$id/${java.net.URLEncoder.encode(name, "UTF-8")}"
}
