package jp.co.studio.kaka.domain.model

data class User(
    val userId: Long,
    val username: String,
    val email: String,
    val avatarUrl: String?,
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}
