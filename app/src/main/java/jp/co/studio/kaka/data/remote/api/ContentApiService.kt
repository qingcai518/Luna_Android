package jp.co.studio.kaka.data.remote.api

import jp.co.studio.kaka.data.remote.dto.ApiResponse
import jp.co.studio.kaka.data.remote.dto.ArtistListDto
import jp.co.studio.kaka.data.remote.dto.CategoryListDto
import jp.co.studio.kaka.data.remote.dto.LyricsDto
import jp.co.studio.kaka.data.remote.dto.MusicListDto
import jp.co.studio.kaka.data.remote.dto.RecommendationDto
import jp.co.studio.kaka.data.remote.dto.SearchResponseDto
import jp.co.studio.kaka.data.remote.dto.UserEventDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * All non-auth endpoints. Kept in one interface alongside [AuthApiService] (i.e. exactly two
 * Retrofit service interfaces total, not three+) - splitting the API surface across three or
 * more `retrofit.create()`-backed interfaces in this module reproducibly breaks KSP's Hilt
 * dependency-injection processing ("X could not be resolved" for the interface type), confirmed
 * by bisection on this toolchain (Kotlin 2.2.10 / KSP 2.2.10-2.0.2 / Hilt 2.60.1). Two
 * interfaces with the full method set works fine - see [[build-config-gotchas]] memory.
 */
interface ContentApiService {

    @GET("artist")
    suspend fun getArtists(): ApiResponse<ArtistListDto>

    @GET("category")
    suspend fun getCategories(): ApiResponse<CategoryListDto>

    @GET("music/artist")
    suspend fun getMusicByArtist(@Query("artistId") artistId: Long): ApiResponse<MusicListDto>

    @GET("music/category")
    suspend fun getMusicByCategory(@Query("categoryId") categoryId: Long): ApiResponse<MusicListDto>

    @GET("search")
    suspend fun search(
        @Query("keyword") keyword: String,
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): ApiResponse<SearchResponseDto>

    @GET("lyrics")
    suspend fun getLyrics(@Query("musicId") musicId: Long): ApiResponse<LyricsDto>

    @GET("recommendations")
    suspend fun getRecommendations(
        @Query("scene") scene: String,
        @Query("limit") limit: Int,
    ): ApiResponse<List<RecommendationDto>>

    @POST("events/batch")
    suspend fun postEventsBatch(@Body events: List<UserEventDto>): ApiResponse<JsonElement?>
}
