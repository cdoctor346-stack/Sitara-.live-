package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_vods")
data class VodEntity(
    @PrimaryKey val id: String,
    val title: String,
    val streamerName: String,
    val durationString: String,
    val dateString: String,
    val viewsCount: Int,
    val category: String
)

@Entity(tableName = "stream_settings")
data class StreamSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val rtmpUrl: String,
    val streamKey: String,
    val preferredResolution: String,
    val preferredBitrate: Int,
    val userCoins: Int,
    val userDiamonds: Int
)

@Dao
interface StreamDao {
    @Query("SELECT * FROM saved_vods ORDER BY dateString DESC")
    fun getAllVods(): Flow<List<VodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVod(vod: VodEntity)

    @Query("DELETE FROM saved_vods WHERE id = :vodId")
    suspend fun deleteVod(vodId: String)

    @Query("SELECT * FROM stream_settings WHERE id = 1")
    fun getSettings(): Flow<StreamSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: StreamSettingsEntity)
}

@Database(entities = [VodEntity::class, StreamSettingsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun streamDao(): StreamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sitara_live_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
