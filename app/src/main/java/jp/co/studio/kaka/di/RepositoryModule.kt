package jp.co.studio.kaka.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.co.studio.kaka.data.local.datastore.SecureTokenStore
import jp.co.studio.kaka.data.local.datastore.TokenStore
import jp.co.studio.kaka.data.repository.ArtistRepositoryImpl
import jp.co.studio.kaka.data.repository.AuthRepositoryImpl
import jp.co.studio.kaka.data.repository.CategoryRepositoryImpl
import jp.co.studio.kaka.data.repository.DownloadRepositoryImpl
import jp.co.studio.kaka.data.repository.EventRepositoryImpl
import jp.co.studio.kaka.data.repository.LyricsRepositoryImpl
import jp.co.studio.kaka.data.repository.MusicRepositoryImpl
import jp.co.studio.kaka.data.repository.RecommendationRepositoryImpl
import jp.co.studio.kaka.data.repository.SearchRepositoryImpl
import jp.co.studio.kaka.data.repository.SettingsRepositoryImpl
import jp.co.studio.kaka.domain.repository.ArtistRepository
import jp.co.studio.kaka.domain.repository.AuthRepository
import jp.co.studio.kaka.domain.repository.CategoryRepository
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.domain.repository.EventRepository
import jp.co.studio.kaka.domain.repository.LyricsRepository
import jp.co.studio.kaka.domain.repository.MusicRepository
import jp.co.studio.kaka.domain.repository.RecommendationRepository
import jp.co.studio.kaka.domain.repository.SearchRepository
import jp.co.studio.kaka.domain.repository.SettingsRepository
import jp.co.studio.kaka.player.MediaControllerRepository
import jp.co.studio.kaka.player.MediaControllerRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindArtistRepository(impl: ArtistRepositoryImpl): ArtistRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(impl: RecommendationRepositoryImpl): RecommendationRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(impl: LyricsRepositoryImpl): LyricsRepository

    @Binds
    @Singleton
    abstract fun bindTokenStore(impl: SecureTokenStore): TokenStore

    @Binds
    @Singleton
    abstract fun bindMediaControllerRepository(impl: MediaControllerRepositoryImpl): MediaControllerRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
