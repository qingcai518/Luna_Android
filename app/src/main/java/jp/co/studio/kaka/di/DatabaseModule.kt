package jp.co.studio.kaka.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.co.studio.kaka.data.local.db.LunaDatabase
import jp.co.studio.kaka.data.local.db.MIGRATIONS
import jp.co.studio.kaka.data.local.db.dao.DownloadedMusicDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLunaDatabase(@ApplicationContext context: Context): LunaDatabase =
        Room.databaseBuilder(context, LunaDatabase::class.java, "luna.db")
            .addMigrations(*MIGRATIONS)
            .build()

    @Provides
    @Singleton
    fun provideDownloadedMusicDao(database: LunaDatabase): DownloadedMusicDao = database.downloadedMusicDao()
}
