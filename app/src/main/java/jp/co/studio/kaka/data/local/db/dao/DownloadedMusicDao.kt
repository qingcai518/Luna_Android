package jp.co.studio.kaka.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jp.co.studio.kaka.data.local.db.entity.DownloadedMusicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedMusicDao {

    @Query("SELECT * FROM downloaded_music ORDER BY downloadDate DESC")
    fun observeAll(): Flow<List<DownloadedMusicEntity>>

    @Query("SELECT * FROM downloaded_music ORDER BY downloadDate DESC")
    suspend fun getAllOnce(): List<DownloadedMusicEntity>

    @Query("SELECT * FROM downloaded_music WHERE id = :id")
    suspend fun getById(id: Long): DownloadedMusicEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_music WHERE id = :id)")
    suspend fun exists(id: Long): Boolean

    @Upsert
    suspend fun upsert(entity: DownloadedMusicEntity)

    @Query("DELETE FROM downloaded_music WHERE id = :id")
    suspend fun deleteById(id: Long)
}
