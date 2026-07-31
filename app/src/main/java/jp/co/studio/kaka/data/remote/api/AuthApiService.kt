package jp.co.studio.kaka.data.remote.api

import jp.co.studio.kaka.data.remote.dto.ApiResponse
import jp.co.studio.kaka.data.remote.dto.LoginRequest
import jp.co.studio.kaka.data.remote.dto.RefreshTokenRequest
import jp.co.studio.kaka.data.remote.dto.RegisterRequest
import jp.co.studio.kaka.data.remote.dto.TokenResponseDto
import jp.co.studio.kaka.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("users/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<UserResponseDto>

    @POST("users/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<String?>

    @POST("users/refresh-token")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): ApiResponse<TokenResponseDto>
}
