package jp.co.studio.kaka.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Unified backend response envelope: {code, message, data}. Success is code == "000000".
 * `data` is nullable because register/events-batch endpoints always return null data.
 */
@Serializable
data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T? = null,
)
