package com.crosslens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosslens.app.data.local.entity.FeedMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedMetadataDao {
    @Query("SELECT * FROM feed_metadata WHERE id = 'live_feed' LIMIT 1")
    suspend fun getFeedMetadata(): FeedMetadataEntity?

    @Query("SELECT * FROM feed_metadata WHERE id = 'live_feed' LIMIT 1")
    fun observeFeedMetadata(): Flow<FeedMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: FeedMetadataEntity)

    @Query("DELETE FROM feed_metadata")
    suspend fun deleteAll()
}
