package jp.co.studio.kaka.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val avatarUrl: String = "",
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

/**
 * Flat response shape confirmed against LunaAPI's UserResponse.java - there is NO nested
 * `user` object and NO tokenType/expiresIn fields, unlike what the iOS client's decoder
 * defensively (and unnecessarily) supports.
 */
@Serializable
data class UserResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val userid: Long,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
)

@Serializable
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
)
