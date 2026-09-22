package com.crosslens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosslens.app.data.local.entity.SourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources")
    fun observeAllSources(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM sources WHERE id IN (:sourceIds)")
    suspend fun getSourcesByIds(sourceIds: List<String>): List<SourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<SourceEntity>)

    @Query("DELETE FROM sources")
    suspend fun deleteAllSources()
}
