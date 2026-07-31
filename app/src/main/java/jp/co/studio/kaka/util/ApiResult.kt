package jp.co.studio.kaka.util

import jp.co.studio.kaka.data.remote.dto.ApiResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/** Result of a backend call, distinguishing business errors from transport-level failures. */
sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>
    data class Error(val code: String, val message: String) : ApiResult<Nothing>
    data class NetworkError(val throwable: Throwable) : ApiResult<Nothing>
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

/**
 * Minimal envelope used only to read `code`/`message` out of an HTTP error body.
 * The backend has two distinct 401 shapes: JwtAuthenticationFilter writes an EMPTY body,
 * GlobalExceptionHandler writes a JSON body like {"code":"100001","message":"..."}.
 * Parsing must tolerate both - never assume the error body is present or well-formed.
 */
@Serializable
private data class ErrorEnvelope(val code: String = "", val message: String = "")

/**
 * Wraps a Retrofit suspend call returning [ApiResponse] into an [ApiResult], normalizing
 * business errors (code != "000000"), HTTP errors (with possibly-empty error bodies),
 * and network/IO failures into one type repositories can switch on.
 */
suspend fun <T> safeApiCall(json: Json, call: suspend () -> ApiResponse<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.code == Constants.API_SUCCESS_CODE) {
            @Suppress("UNCHECKED_CAST")
            ApiResult.Success(response.data as T)
        } else {
            ApiResult.Error(response.code, response.message)
        }
    } catch (e: HttpException) {
        val errorEnvelope = runCatching { e.response()?.errorBody()?.string() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.let { body -> runCatching { json.decodeFromString<ErrorEnvelope>(body) }.getOrNull() }
        ApiResult.Error(
            code = errorEnvelope?.code?.takeIf { it.isNotBlank() } ?: e.code().toString(),
            message = errorEnvelope?.message?.takeIf { it.isNotBlank() } ?: (e.message() ?: "HTTP ${e.code()}"),
        )
    } catch (e: IOException) {
        ApiResult.NetworkError(e)
    }
}
