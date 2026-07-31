package jp.co.studio.kaka.ui.profile

data class ProfileUiState(
    val username: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val isLoggedIn: Boolean = false,
    val downloadedCount: Int = 0,
)
