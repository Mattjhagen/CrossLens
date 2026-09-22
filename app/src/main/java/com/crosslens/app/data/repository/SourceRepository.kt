package com.crosslens.app.data.repository

import com.crosslens.app.core.model.Source
import kotlinx.coroutines.flow.Flow

interface SourceRepository {
    fun observeSources(): Flow<List<Source>>
    suspend fun getSourcesByIds(sourceIds: List<String>): List<Source>
}
