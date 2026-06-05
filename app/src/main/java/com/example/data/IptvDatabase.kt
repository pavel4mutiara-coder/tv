package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// Entities
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @androidx.room.PrimaryKey val channelId: String
)

@Entity(tableName = "custom_playlists")
data class PlaylistEntity(
    @androidx.room.PrimaryKey val playlistUrl: String,
    val playlistName: String,
    val addedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_channels")
data class CustomChannelEntity(
    @androidx.room.PrimaryKey val streamUrl: String,
    val name: String,
    val logoUrl: String,
    val category: String,
    val country: String,
    val playlistUrl: String
)

// DAO
@Dao
interface IptvDao {
    // Favorites
    @Query("SELECT * FROM favorites")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE channelId = :channelId")
    suspend fun removeFavorite(channelId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE channelId = :channelId LIMIT 1)")
    suspend fun isFavoriteSync(channelId: String): Boolean

    // Playlists
    @Query("SELECT * FROM custom_playlists ORDER BY addedTime DESC")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM custom_playlists WHERE playlistUrl = :url")
    suspend fun deletePlaylist(url: String)

    // Custom Channels
    @Query("SELECT * FROM custom_channels")
    fun getCustomChannels(): Flow<List<CustomChannelEntity>>

    @Query("SELECT * FROM custom_channels WHERE playlistUrl = :playlistUrl")
    suspend fun getChannelsByPlaylist(playlistUrl: String): List<CustomChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCustomChannels(channels: List<CustomChannelEntity>)

    @Query("DELETE FROM custom_channels WHERE playlistUrl = :playlistUrl")
    suspend fun deleteCustomChannelsByPlaylist(playlistUrl: String)
}

// Database
@Database(
    entities = [FavoriteEntity::class, PlaylistEntity::class, CustomChannelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class IptvDatabase : RoomDatabase() {
    abstract fun iptvDao(): IptvDao
}
