package com.crosslens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosslens.app.data.local.entity.FrameObservationEntity

@Dao
interface FrameObservationDao {
    @Query("SELECT * FROM frame_observations WHERE storyId = :storyId")
    suspend fun getObservationsByStory(storyId: String): List<FrameObservationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservations(observations: List<FrameObservationEntity>)

    @Query("DELETE FROM frame_observations")
    suspend fun deleteAllObservations()
}
