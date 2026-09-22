package com.crosslens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosslens.app.data.local.entity.ClaimEntity

@Dao
interface ClaimDao {
    @Query("SELECT * FROM claims WHERE id IN (:claimIds)")
    suspend fun getClaimsByIds(claimIds: List<String>): List<ClaimEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaims(claims: List<ClaimEntity>)

    @Query("DELETE FROM claims")
    suspend fun deleteAllClaims()
}
