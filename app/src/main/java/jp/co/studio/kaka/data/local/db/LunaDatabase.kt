package jp.co.studio.kaka.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import jp.co.studio.kaka.data.local.db.dao.DownloadedMusicDao
import jp.co.studio.kaka.data.local.db.entity.DownloadedMusicEntity

@Database(entities = [DownloadedMusicEntity::class], version = 1, exportSchema = true)
abstract class LunaDatabase : RoomDatabase() {
    abstract fun downloadedMusicDao(): DownloadedMusicDao
}
