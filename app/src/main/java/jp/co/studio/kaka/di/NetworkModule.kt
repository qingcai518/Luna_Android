package jp.co.studio.kaka.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.co.studio.kaka.BuildConfig
import jp.co.studio.kaka.data.remote.api.AuthApiService
import jp.co.studio.kaka.data.remote.api.ContentApiService
import jp.co.studio.kaka.data.remote.converter.SerializationConverterFactory
import jp.co.studio.kaka.data.remote.interceptor.AuthInterceptor
import jp.co.studio.kaka.data.remote.interceptor.TokenAuthenticator
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

/** Authenticated client used by [AuthApiService]/[ContentApiService] - carries the Bearer token and 401 refresh logic. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiClient

/**
 * Plain client for downloading OSS/CDN-signed file URLs (audio/cover/lyrics). Deliberately has
 * no Bearer header or 401-refresh logic: a 403 from these URLs means the 1-hour signature
 * expired, which is fixed by re-fetching a fresh MusicVO/LyricsVO, not by refreshing login state.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    @ApiClient
    @Provides
    @Singleton
    fun provideApiOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(loggingInterceptor)
        .build()

    @DownloadClient
    @Provides
    @Singleton
    fun provideDownloadOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(@ApiClient okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                SerializationConverterFactory.create(json, "application/json".toMediaType()),
            )
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideContentApiService(retrofit: Retrofit): ContentApiService =
        retrofit.create(ContentApiService::class.java)
}
